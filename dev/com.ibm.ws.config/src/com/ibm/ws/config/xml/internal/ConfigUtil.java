/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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

package com.ibm.ws.config.xml.internal;

import java.io.Closeable;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;

class ConfigUtil {

    private static final TraceComponent tc = Tr.register(ConfigUtil.class, XMLConfigConstants.TR_GROUP, XMLConfigConstants.NLS_PROPS);

    @FFDCIgnore(Throwable.class)
    static public void closeIO(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
        }
    }

    static public boolean exists(final File file) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return file.exists();
            }
        });
    }

    static public void delete(final File file) {
        boolean success = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return file.delete();
            }
        });
        if (!success && tc.isWarningEnabled()) {
            Tr.warning(tc, "warn.file.delete.failed", file);
        }
    }

    static public void mkdirs(final File file) {
        boolean success = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return file.mkdirs();
            }
        });
        if (!success && tc.isWarningEnabled()) {
            Tr.warning(tc, "warn.file.mkdirs.failed", file);
        }
    }

    static public String getSystemProperty(final String name) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty(name);
            }
        });
    }
}
