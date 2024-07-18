/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.microprofile.telemetry20.logging.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import com.ibm.websphere.logging.WsLevel;
import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.logging.collector.CollectorConstants;
import com.ibm.ws.logging.collector.CollectorJsonHelpers;
import com.ibm.ws.logging.collector.CollectorJsonUtils;
import com.ibm.ws.logging.collector.Formatter;
import com.ibm.ws.logging.collector.LogFieldConstants;
import com.ibm.ws.logging.data.GenericData;
import com.ibm.ws.logging.data.KeyValuePair;
import com.ibm.ws.logging.data.KeyValuePairList;
import com.ibm.ws.logging.data.LogTraceData;
import com.ibm.wsspi.collector.manager.BufferManager;
import com.ibm.wsspi.collector.manager.CollectorManager;
import com.ibm.wsspi.collector.manager.Handler;
import com.ibm.wsspi.collector.manager.SynchronousHandler;

import io.openliberty.microprofile.telemetry.internal.common.constants.OpenTelemetryConstants;
import io.openliberty.microprofile.telemetry.internal.common.info.OpenTelemetryInfo;
import io.openliberty.microprofile.telemetry.internal.interfaces.OpenTelemetryAccessor;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.semconv.SemanticAttributes;

@Component(name = OpenTelemetryLogHandler.COMPONENT_NAME, service = { Handler.class }, configurationPolicy = ConfigurationPolicy.OPTIONAL, property = { "service.vendor=IBM" })
public class OpenTelemetryLogHandler implements SynchronousHandler, Formatter {

    private static final TraceComponent tc = Tr.register(OpenTelemetryLogHandler.class, "TELEMETRY", "io.openliberty.microprofile.telemetry.internal.common.resources.MPTelemetry");

    public static final String COMPONENT_NAME = "io.openliberty.microprofile.telemetry20.logging.internal.OpenTelemetryLogHandler";

    protected static volatile boolean isInit = false;;

    protected CollectorManager collectorMgr = null;

    protected static final String SOURCE_LIST_KEY = "source";

    protected final int MAXFIELDLENGTH = -1; //Unlimited field length

    private OpenTelemetryInfo openTelemetry;

    private List<String> sourcesList = new ArrayList<String>();

    @Activate
    protected void activate(ComponentContext cc, Map<String, Object> configuration) {
        if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
            Tr.debug(tc, "In activate()");
        }

        this.openTelemetry = OpenTelemetryAccessor.getOpenTelemetryInfo();

        // Validate the configured sources
        validateSources(configuration);

        // Configure the sources
        List<String> configSourceList = getConfiguredSourcesList(configuration);
        this.sourcesList = configSourceList;
    }

    @Deactivate
    protected void deactivate(ComponentContext cc, int reason) {
        if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
            Tr.debug(tc, "In deactivate()");
        }
        //To ensure that we don't hold any reference to the collector manager after
        //the component gets deactivated.
        collectorMgr = null;
    }

    @SuppressWarnings("unchecked")
    @Modified
    protected void modified(Map<String, Object> configuration) {
        if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
            Tr.debug(tc, "In modified()");
        }

        // Validate the configured sources
        validateSources(configuration);

        List<String> newSources = getConfiguredSourcesList(configuration);

        if (collectorMgr == null || isInit == false) {
            this.sourcesList = newSources;
            return;
        }

        try {
            //Old sources
            ArrayList<String> oldSources = new ArrayList<String>(sourcesList);

            //Sources to remove -> In Old Sources, the difference between oldSource and newSource
            ArrayList<String> sourcesToRemove = new ArrayList<String>(oldSources);
            sourcesToRemove.removeAll(newSources);
            collectorMgr.unsubscribe(this, convertToSourceIDList(sourcesToRemove));

            //Sources to Add -> In New Sources, the difference bewteen newSource and oldSource
            ArrayList<String> sourcesToAdd = new ArrayList<String>(newSources);
            sourcesToAdd.removeAll(oldSources);
            collectorMgr.subscribe(this, convertToSourceIDList(sourcesToAdd));

            sourcesList = newSources; //new primary sourcesList
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /** {@inheritDoc} */
    @Override
    public void init(CollectorManager collectorManager) {
        try {
            this.collectorMgr = collectorManager;
            collectorMgr.subscribe(this, convertToSourceIDList(sourcesList));
            isInit = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<String> getConfiguredSourcesList(Map<String, Object> configuration) {
        List<String> srcList = new ArrayList<String>();
        if (configuration.containsKey(SOURCE_LIST_KEY)) {
            String[] sourceList = (String[]) configuration.get(SOURCE_LIST_KEY);
            srcList = Arrays.asList(sourceList);
        }
        return srcList;
    }

    private void validateSources(Map<String, Object> config) {
        if (config.containsKey(SOURCE_LIST_KEY)) {
            String[] sourceList = (String[]) config.get(SOURCE_LIST_KEY);
            if (sourceList != null) {
                for (String source : sourceList) {
                    if (getSourceName(source.trim()).isEmpty()) {
                        Tr.warning(tc, "CWMOT5005.mptelemetry.unknown.log.source", source);
                    }
                }
            }
        }
    }

    /*
     * Get the fully qualified source string from the config value
     */
    protected String getSourceName(String source) {
        if (source.equals(CollectorConstants.MESSAGES_CONFIG_VAL))
            return CollectorConstants.MESSAGES_SOURCE;
        else if (source.equals(CollectorConstants.TRACE_CONFIG_VAL))
            return CollectorConstants.TRACE_SOURCE;
        else if (source.equals(CollectorConstants.FFDC_CONFIG_VAL))
            return CollectorConstants.FFDC_SOURCE;

        return "";
    }

    /*
     * Given the sourceList in 'config' form, return the sourceID
     * Proper sourceID format is <source> | <location>
     */
    protected List<String> convertToSourceIDList(List<String> sourceList) {
        List<String> sourceIDList = new ArrayList<String>();
        for (String source : sourceList) {
            String sourceName = getSourceName(source);
            if (!sourceName.equals("")) {
                if (!sourceName.contains(CollectorConstants.AUDIT_CONFIG_VAL)) {
                    sourceIDList.add(getSourceName(source) + "|" + CollectorConstants.MEMORY);
                } else {
                    sourceIDList.add(getSourceName(source) + "|" + CollectorConstants.SERVER);
                }
            }
        }
        return sourceIDList;
    }

    @Override
    public String getHandlerName() {
        return COMPONENT_NAME;
    }

    @Override
    public Object formatEvent(String source, String location, Object event, String[] tags, int maxFieldLength) {
        LogRecordBuilder builder = null;
        String eventType = CollectorJsonUtils.getEventType(source, location);
        if (eventType.equals(CollectorConstants.MESSAGES_LOG_EVENT_TYPE)) {
            LogTraceData logData = (LogTraceData) event;
            if (logData.getMessage().contains("scopeInfo:")) {
                return null;
            }

            // Get Attributes builder to add additional Log fields
            AttributesBuilder attributes = Attributes.builder();

            // Get Extensions (LogRecordContext) from LogData and add it as attributes.
            ArrayList<KeyValuePair> extensions = null;
            KeyValuePairList kvpl = null;
            kvpl = logData.getExtensions();
            if (kvpl != null) {
                if (kvpl.getKey().equals(LogFieldConstants.EXTENSIONS_KVPL)) {
                    extensions = kvpl.getList();
                    for (KeyValuePair k : extensions) {
                        String extKey = k.getKey();
                        if (extKey.endsWith(CollectorJsonHelpers.INT_SUFFIX)) {
                            attributes.put(extKey, k.getIntValue());
                        } else if (extKey.endsWith(CollectorJsonHelpers.FLOAT_SUFFIX)) {
                            attributes.put(extKey, k.getFloatValue());
                        } else if (extKey.endsWith(CollectorJsonHelpers.LONG_SUFFIX)) {
                            attributes.put(extKey, k.getLongValue());
                        } else if (extKey.endsWith(CollectorJsonHelpers.BOOL_SUFFIX)) {
                            attributes.put(extKey, k.getBooleanValue());
                        } else {
                            attributes.put(extKey, k.getStringValue());
                        }
                    }
                }
            }

            OpenTelemetryInfo otelInstance = null;

            if (OpenTelemetryAccessor.isRuntimeEnabled()) {
                otelInstance = openTelemetry;
            } else {
                otelInstance = OpenTelemetryAccessor.getOpenTelemetryInfo();
            }

            if (otelInstance != null && otelInstance.getEnabled()) {
                builder = otelInstance.getOpenTelemetry().getLogsBridge().loggerBuilder(OpenTelemetryConstants.INSTRUMENTATION_NAME).build().logRecordBuilder();
            }

            if (builder != null)
                mapLibertyLogRecordToOTelLogRecord(builder, logData, eventType, attributes);
        }
        return builder;
    }

    /** {@inheritDoc} */
    @Override
    public void synchronousWrite(Object event) {
        /*
         * Given an 'object' we must determine what type of log event it originates from.
         * Knowing that it is a *Data object, we can figure what type of source it is.
         */
        GenericData genData = null;
        if (event instanceof GenericData) {
            genData = (GenericData) event;
        } else {
            throw new IllegalArgumentException("event not an instance of GenericData");
        }

        String eventSourceName = getSourceNameFromDataObject(genData);
        LogRecordBuilder builder = null;
        if (!eventSourceName.isEmpty()) {
            builder = (LogRecordBuilder) formatEvent(eventSourceName, CollectorConstants.MEMORY, event, null, MAXFIELDLENGTH);
        }
        if (builder != null) {
            // Emit the constructed logs to the configured OpenTelemetry Logs Exporter
            builder.emit();
        }

    }

    protected String getSourceNameFromDataObject(Object event) {

        GenericData genData = (GenericData) event;
        String sourceName = genData.getSourceName();

        if (sourceName.equals(CollectorConstants.MESSAGES_SOURCE)) {
            return CollectorConstants.MESSAGES_SOURCE;
        } else if (sourceName.equals(CollectorConstants.TRACE_SOURCE)) {
            return CollectorConstants.TRACE_SOURCE;
        } else if (sourceName.equals(CollectorConstants.FFDC_SOURCE)) {
            return CollectorConstants.FFDC_SOURCE;
        } else {
            return "";
        }
    }

    private void mapLibertyLogRecordToOTelLogRecord(LogRecordBuilder builder, LogTraceData logData, String eventType, AttributesBuilder attributes) {
        boolean isMessageEvent = eventType.equals(CollectorConstants.MESSAGES_LOG_EVENT_TYPE);

        // Get message from LogData and set it in the LogRecordBuilder
        String message = logData.getMessage();
        builder.setBody(message);

        // Get Timestamp from LogData and set it in the LogRecordBuilder
        builder.setTimestamp(logData.getDatetime(), TimeUnit.MILLISECONDS);

        // Get Log Level from LogData and set it in the LogRecordBuilder
        String loglevel = logData.getLoglevel();
        builder.setSeverity(wsLevelToSeverity(loglevel));

        // Get Log Severity from LogData and set it in the LogRecordBuilder
        String logSeverity = logData.getSeverity();
        builder.setSeverityText(logSeverity);

        // Add Thread information to Attributes Builder
        attributes.put(SemanticAttributes.THREAD_NAME, logData.getThreadName());
        attributes.put(SemanticAttributes.THREAD_ID, logData.getThreadId());

        // Add Throwable information to Attribute Builder
        String exceptionName = logData.getExceptionName();
        String throwable = logData.getThrowable();
        if (throwable != null) {
            attributes.put(SemanticAttributes.EXCEPTION_MESSAGE, exceptionName);
            attributes.put(SemanticAttributes.EXCEPTION_STACKTRACE, throwable);
        }

        // Add additional log information from LogData to Attributes Builder
        attributes.put(LogTraceData.getModuleKey(0, isMessageEvent), logData.getModule())
                        .put(LogTraceData.getMessageIdKey(0, isMessageEvent), logData.getMessageId())
                        .put(LogTraceData.getMethodNameKey(0, isMessageEvent), logData.getMethodName())
                        .put(LogTraceData.getClassNameKey(0, isMessageEvent), logData.getClassName())
                        .put(LogTraceData.getSequenceKey(0, isMessageEvent), logData.getSequence());

        // Set the Attributes to the builder.
        builder.setAllAttributes(attributes.build());

        // span context
        //builder.setContext(Context.current());
    }

    private static Severity wsLevelToSeverity(String level) {
        if (level.equals(WsLevel.FATAL.toString())) {
            return Severity.FATAL;
        } else if (level.equals(WsLevel.SEVERE.toString()) || level.equals("SystemErr")) {
            return Severity.ERROR;
        } else if (level.equals(WsLevel.WARNING.toString())) {
            return Severity.WARN;
        } else if (level.equals(WsLevel.AUDIT.toString())) {
            return Severity.INFO2;
        } else if (level.equals(WsLevel.INFO.toString())) {
            return Severity.INFO;
        } else if (level.equals(WsLevel.CONFIG.toString()) || level.equals("SystemOut")) {
            return Severity.DEBUG4;
        } else if (level.equals(WsLevel.DETAIL.toString())) {
            return Severity.DEBUG3;
        } else if (level.equals(WsLevel.FINE.toString())) {
            return Severity.DEBUG2;
        } else if (level.equals(WsLevel.FINER.toString())) {
            return Severity.DEBUG;
        } else if (level.equals(WsLevel.FINEST.toString())) {
            return Severity.TRACE;
        } else {
            return Severity.FATAL;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unsetBufferManager(String arg0, BufferManager arg1) {
        //Not needed in a Synchronized Handler
    }

    /** {@inheritDoc} */
    @Override
    public void setBufferManager(String arg0, BufferManager arg1) {
        //Not needed in a Synchronized Handler
    }
}