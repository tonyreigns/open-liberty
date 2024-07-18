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

/**
 * This class contains the transformed Liberty event field names to match OpenTelemetry Log Attributes naming convention.
 */
public class TelemetryLogFieldConstants {

    //Common Mapped OTel Attribute Log fields
    public static final String IBM_TYPE = "com.ibm.type";
    public static final String IBM_USERDIR = "com.ibm.user_dir";
    public static final String IBM_SERVERNAME = "com.ibm.server_name";
    public static final String IBM_SEQUENCE = "com.ibm.sequence";
    public static final String IBM_CLASSNAME = "com.ibm.class_name";

    //Mapped OTel Attribute Liberty message and trace log fields
    public static final String IBM_MESSAGEID = "com.ibm.message_id";
    public static final String IBM_MODULE = "com.ibm.module";
    //public static final String LOGGERNAME = "loggerName";
    public static final String IBM_METHODNAME = "com.ibm.method_name";

    //Mapped OTel Attribute Liberty FFDC log fields
    public static final String IBM_PROBEID = "com.ibm.probe_id";
    public static final String IBM_OBJECTDETAILS = "com.ibm.object_details";

    //Mapped OTel Attribute Liberty LogRecordContext Extension fields
    public static final String EXT_APP_NAME = "com.ibm.ext.app_name";

    // Miscellaneous
    public static final String EXT_PREFIX = "ext_";

    // OpenTelemetry Scope Info field
    public static final String OTEL_SCOPE_INFO = "scopeInfo:";
}
