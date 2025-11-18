/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/


package com.ibm.ws.logging.internal.osgi;


import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.ibm.wsspi.logging.Introspector;

import com.ibm.ws.logging.internal.impl.BaseTraceService;

import com.ibm.ws.logging.internal.impl.ThrottleState;
import com.ibm.ws.logging.internal.osgi.stackjoiner.MethodProxy;
import com.ibm.ws.logging.internal.osgi.stackjoiner.boot.templates.ThrowableProxy;

/**
 *
 */
//@Component(service =  Introspector.class ,
//           immediate = true,
//           configurationPolicy = ConfigurationPolicy.IGNORE,
//           property = {"service.vendor=IBM"
//           })
public class LogThrottleIntrospector implements Introspector {

//    @Activate
//    protected void activate(BundleContext context) {
//        //System.out.println("HELLLOOOO2222");
//    }
    private MethodProxy methodProxy;
    final String BASE_TRACE_SERVICE_CLASS_NAME = "com.ibm.ws.logging.internal.impl.BaseTraceService";
    final String BASE_TRACE_SERVICE_METHOD_NAME = "testMethod";
    private static Instrumentation instrumentation = null;

    @Override
    public String getIntrospectorName() {
        return "LogThrottleIntrospector";
    }

    @Override
    public String getIntrospectorDescription() {
        return "List of logs being throttled.";
    }

    public void init() {
        instrumentation = Activator.getInstrumentation();
    }
    
    @Override
    public void introspect(PrintWriter out) throws Exception {
        out.println("~~~~~~~~~~~~~~~~~~~");
        
        if (methodProxy == null) {
            /*
             * Create a methodProxy of the printStackTraceOverride method within the
             * BaseTraceService Class
             */
            methodProxy = new MethodProxy(instrumentation, BASE_TRACE_SERVICE_CLASS_NAME,
                    BASE_TRACE_SERVICE_METHOD_NAME, Throwable.class, PrintStream.class);
            if (!methodProxy.isInitialized()) {
                methodProxy = null;
               // return;
            }
        }
        
        
        if (methodProxy != null) {
        	
        	System.out.println("Method proxy: " + methodProxy.getMethodProxy());
        	out.println("Method proxy: " + methodProxy.getMethodProxy());

        }
        else {
        	out.println("Method proxy is null");
        }
        
        
        BaseTraceService test = new BaseTraceService();

        ThrowableProxy test2 = new ThrowableProxy( );
        
        test2.setFireTarget(BASE_TRACE_SERVICE_CLASS_NAME, test.getClass().getMethod(BASE_TRACE_SERVICE_METHOD_NAME));
        
        
        test2.fireMethod();


        for (Map.Entry<String, ThrottleState> entry : test.throttleStates.entrySet()) {
            out.println("Key being throttled: " + entry.getKey() + " -- Occurences over the last 5 minutes: " + entry.getValue() + " -- Last occurence: "
                        + entry.getValue().getLastAccessTime());

        }
        out.println("~~~~~~~~~~~~~~~~~~~");
    }
}
