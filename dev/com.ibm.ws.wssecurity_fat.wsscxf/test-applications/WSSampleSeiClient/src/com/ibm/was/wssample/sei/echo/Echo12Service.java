/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

//
// Generated By:JAX-WS RI IBM 2.2.1-11/28/2011 08:27 AM(foreman)- (JAXB RI IBM 2.2.3-11/28/2011 06:17 AM(foreman)-)
//

package com.ibm.was.wssample.sei.echo;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient(name = "Echo12Service", targetNamespace = "http://com/ibm/was/wssample/sei/echo/", wsdlLocation = "WEB-INF/wsdl/EchoBsp.wsdl")
public class Echo12Service extends Service {

    private final static URL ECHOSERVICE_WSDL_LOCATION;
    private final static WebServiceException ECHOSERVICE_EXCEPTION;
    private final static QName ECHOSERVICE_QNAME = new QName("http://com/ibm/was/wssample/sei/echo/", "Echo12Service");

    static {
        ECHOSERVICE_WSDL_LOCATION = com.ibm.was.wssample.sei.echo.Echo12Service.class.getResource("/WEB-INF/wsdl/EchoBsp.wsdl");
        WebServiceException e = null;
        if (ECHOSERVICE_WSDL_LOCATION == null) {
            e = new WebServiceException("Cannot find 'WEB-INF/wsdl/EchoBsp.wsdl' wsdl. Place the resource correctly in the classpath.");
        }
        ECHOSERVICE_EXCEPTION = e;
    }

    public Echo12Service() {
        super(__getWsdlLocation(), ECHOSERVICE_QNAME);
    }

    public Echo12Service(WebServiceFeature... features) {
        super(__getWsdlLocation(), ECHOSERVICE_QNAME, features);
    }

    public Echo12Service(URL wsdlLocation) {
        super(wsdlLocation, ECHOSERVICE_QNAME);
    }

    public Echo12Service(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, ECHOSERVICE_QNAME, features);
    }

    public Echo12Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public Echo12Service(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *         returns EchoServicePortType
     */
    @WebEndpoint(name = "Echo12ServicePort")
    public EchoServicePortType getEcho12ServicePort() {
        return super.getPort(new QName("http://com/ibm/was/wssample/sei/echo/", "Echo12ServicePort"), EchoServicePortType.class);
    }

    /**
     *
     * @param features
     *                     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy. Supported features not in the <code>features</code> parameter will have their
     *                     default values.
     * @return
     *         returns EchoServicePortType
     */
    @WebEndpoint(name = "Echo12ServicePort")
    public EchoServicePortType getEcho12ServicePort(WebServiceFeature... features) {
        return super.getPort(new QName("http://com/ibm/was/wssample/sei/echo/", "Echo12ServicePort"), EchoServicePortType.class, features);
    }

    private static URL __getWsdlLocation() {
        if (ECHOSERVICE_EXCEPTION != null) {
            throw ECHOSERVICE_EXCEPTION;
        }
        return ECHOSERVICE_WSDL_LOCATION;
    }

}
