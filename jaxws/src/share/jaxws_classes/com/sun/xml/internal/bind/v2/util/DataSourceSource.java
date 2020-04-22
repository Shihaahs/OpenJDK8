/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.xml.internal.bind.v2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * {@link Source} implementation backed by {@link DataHandler}.
 *
 * <p>
 * This implementation allows the same {@link Source} to be used
 * mutliple times.
 *
 * <p>
 * {@link Source} isn't really pluggable. As a consequence,
 * this implementation is clunky --- weak against unexpected
 * usage of the class.
 *
 * @author Kohsuke Kawaguchi
 */
public final class DataSourceSource extends StreamSource {
    private final DataSource source;

    /**
     * If null, default to the encoding declaration
     */
    private final String charset;

    // remember the value we returned so that the 2nd invocation
    // will return the same object, which is what's expeted out of
    // StreamSource
    private Reader r;
    private InputStream is;

    public DataSourceSource(DataHandler dh) throws MimeTypeParseException {
        this(dh.getDataSource());
    }

    public DataSourceSource(DataSource source) throws MimeTypeParseException {
        this.source = source;

        String ct = source.getContentType();
        if(ct==null) {
            charset = null;
        } else {
            MimeType mimeType = new MimeType(ct);
            this.charset = mimeType.getParameter("charset");
        }
    }

    @Override
    public void setReader(Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getReader() {
        try {
            if(charset==null)   return null;
            if(r==null)
                r = new InputStreamReader(source.getInputStream(),charset);
            return r;
        } catch (IOException e) {
            // argh
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            if(charset!=null)   return null;
            if(is==null)
                is = source.getInputStream();
            return is;
        } catch (IOException e) {
            // argh
            throw new RuntimeException(e);
        }
    }

    public DataSource getDataSource() {
        return source;
    }
}
