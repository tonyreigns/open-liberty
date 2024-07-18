/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.microprofile.telemetry20.logging.internal;

import java.util.concurrent.TimeUnit;

import com.ibm.websphere.logging.WsLevel;
import com.ibm.ws.logging.collector.CollectorConstants;
import com.ibm.ws.logging.data.LogTraceData;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.SemanticAttributes;

/**
 *
 */
public class MpTelemetryLogUtils {

    /**
     * Get the event type from the Liberty log source.
     *
     * @param source The source where the Liberty event originated from.
     */
    public static String getLibertyEventType(String source) {
        if (source.equals(CollectorConstants.MESSAGES_SOURCE)) {
            return CollectorConstants.MESSAGES_LOG_EVENT_TYPE;
        } else if (source.endsWith(CollectorConstants.TRACE_SOURCE)) {
            return CollectorConstants.TRACE_LOG_EVENT_TYPE;
        } else if (source.endsWith(CollectorConstants.FFDC_SOURCE)) {
            return CollectorConstants.FFDC_EVENT_TYPE;
        } else
            return "";
    }

    /**
     * Map the log event data to the OpenTelemetry Logs Data Model Format.
     *
     * @param event          The object originating from logging source which contains necessary fields
     * @param eventType      The type of event
     * @param servername     The name of the server
     * @param wlpUserDir     The name of wlp user directory
     * @param serverHostName The name of server host
     * @param maxFieldLength The max character length of strings
     */
    public static void mapLibertyEventToOpenTelemetry(LogRecordBuilder builder, Object event, String eventType, String serverName, String wlpUserDir, String serverHostName) {
        if (eventType.equals(CollectorConstants.MESSAGES_LOG_EVENT_TYPE)) {
            mapMessageAndTraceToOpenTelemetry(builder, wlpUserDir, serverName, serverHostName, eventType, event);
        } else if (eventType.equals(CollectorConstants.TRACE_LOG_EVENT_TYPE)) {
            mapMessageAndTraceToOpenTelemetry(builder, wlpUserDir, serverName, serverHostName, eventType, event);
        } else if (eventType.equals(CollectorConstants.FFDC_EVENT_TYPE)) {
            mapFFDCToOpenTelemetry(builder, wlpUserDir, serverName, serverHostName, event, eventType);
        }
    }

    /**
     * Maps the Message and Trace log events to the OpenTelemetry Logs Data Model.
     *
     * @param builder
     * @param wlpUserDir
     * @param serverName
     * @param serverHostName
     * @param eventType
     * @param event
     * @return
     */
    private static void mapMessageAndTraceToOpenTelemetry(LogRecordBuilder builder, String wlpUserDir, String serverName, String serverHostName,
                                                          String eventType, Object event) {
        LogTraceData logData = (LogTraceData) event;

        // Check if the event is a mapped OpenTelemetry log event, if it is, skip, to avoid infinite loop.
        if (logData.getMessage().contains(TelemetryLogFieldConstants.OTEL_SCOPE_INFO)) {
            // Do not map an already mapped OTel Log event. Skip...
            builder = null;
            return;
        }

        // Get message from LogData and set it in the LogRecordBuilder
        String message = logData.getMessage();
        builder.setBody(message);

        // Get Timestamp from LogData and set it in the LogRecordBuilder
        builder.setTimestamp(logData.getDatetime(), TimeUnit.MILLISECONDS);

        // Get Log Level from LogData and set it in the LogRecordBuilder
        String loglevel = logData.getLoglevel();
        builder.setSeverity(mapWsLevelToSeverity(loglevel));

        // Get Log Severity from LogData and set it in the LogRecordBuilder
        String logSeverity = logData.getSeverity();
        builder.setSeverityText(logSeverity);

        // Get Attributes builder to add additional Log fields
        AttributesBuilder attributes = Attributes.builder();

        // Add Thread information to Attributes Builder
        attributes.put(SemanticAttributes.THREAD_NAME, logData.getThreadName());
        attributes.put(SemanticAttributes.THREAD_ID, logData.getThreadId());

        // Add Throwable information to Attribute Builder
        String exceptionName = logData.getExceptionName();
        String throwable = logData.getThrowable();
        if (exceptionName != null && throwable != null) {
            attributes.put(SemanticAttributes.EXCEPTION_TYPE, exceptionName);
            attributes.put(SemanticAttributes.EXCEPTION_STACKTRACE, throwable);
        }

        // Add additional log information from LogData to Attributes Builder
        attributes.put(TelemetryLogFieldConstants.IBM_TYPE, eventType)
                        .put(TelemetryLogFieldConstants.IBM_USERDIR, wlpUserDir)
                        .put(TelemetryLogFieldConstants.IBM_SERVERNAME, serverName)
                        .put(TelemetryLogFieldConstants.IBM_MESSAGEID, logData.getMessageId())
                        .put(TelemetryLogFieldConstants.IBM_METHODNAME, logData.getMethodName())
                        .put(TelemetryLogFieldConstants.IBM_MODULE, logData.getModule())
                        .put(TelemetryLogFieldConstants.IBM_CLASSNAME, logData.getClassName())
                        .put(TelemetryLogFieldConstants.IBM_SEQUENCE, logData.getSequence());

        // Set the Attributes to the builder.
        builder.setAllAttributes(attributes.build());

        // Set the Span and Trace IDs from the current context.
        builder.setContext(Context.current());
    }

    /**
     * Maps the FFDC log events to the OpenTelemetry Logs Data Model.
     *
     * @param builder
     * @param maxFieldLength
     * @param wlpUserDir
     * @param serverName
     * @param serverHostName
     * @param event
     * @param eventType
     */
    private static void mapFFDCToOpenTelemetry(LogRecordBuilder builder, String wlpUserDir, String serverName, String serverHostName, Object event,
                                               String eventType) {
        // TODO Auto-generated method stub

    }

    /**
     * Maps the Liberty Log levels to the OpenTelemetry Severity.
     *
     * @param level
     */
    private static Severity mapWsLevelToSeverity(String level) {
        if (level.equals(WsLevel.FATAL.toString())) {
            return Severity.FATAL;
        } else if (level.equals(WsLevel.SEVERE.toString())) {
            return Severity.ERROR;
        } else if (level.equals(WsLevel.WARNING.toString()) || level.equals("SystemErr")) {
            return Severity.WARN;
        } else if (level.equals(WsLevel.AUDIT.toString())) {
            return Severity.INFO2;
        } else if (level.equals(WsLevel.INFO.toString()) || level.equals("SystemOut")) {
            return Severity.INFO;
        } else if (level.equals(WsLevel.CONFIG.toString())) {
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

}
