/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.core5.http.impl.io;

import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;

import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.MessageConstraintException;
import org.apache.hc.core5.http.RequestLine;
import org.apache.hc.core5.http.UnsupportedHttpVersionException;
import org.apache.hc.core5.http.config.MessageConstraints;
import org.apache.hc.core5.http.io.SessionInputBuffer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link DefaultHttpRequestParser}.
 */
public class TestRequestParser {

    @Test
    public void testBasicMessageParsing() throws Exception {
        final String s =
            "GET / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "User-Agent: whatever\r\n" +
            "Cookie: c1=stuff\r\n" +
            "\r\n";
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(s, StandardCharsets.US_ASCII);

        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser();
        final HttpRequest httprequest = parser.parse(inbuffer);

        final RequestLine reqline = httprequest.getRequestLine();
        Assert.assertNotNull(reqline);
        Assert.assertEquals("GET", reqline.getMethod());
        Assert.assertEquals("/", reqline.getUri());
        Assert.assertEquals(HttpVersion.HTTP_1_1, reqline.getProtocolVersion());
        final Header[] headers = httprequest.getAllHeaders();
        Assert.assertEquals(3, headers.length);
    }

    @Test(expected = ConnectionClosedException.class)
    public void testConnectionClosedException() throws Exception {
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(new byte[] {});

        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser();
        parser.parse(inbuffer);
    }

    @Test
    public void testBasicMessageParsingLeadingEmptyLines() throws Exception {
        final String s =
                "\r\n" +
                "\r\n" +
                "GET / HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(s, StandardCharsets.US_ASCII);

        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser(
                MessageConstraints.custom().setMaxEmptyLineCount(3).build());
        final HttpRequest httprequest = parser.parse(inbuffer);

        final RequestLine reqline = httprequest.getRequestLine();
        Assert.assertNotNull(reqline);
        Assert.assertEquals("GET", reqline.getMethod());
        Assert.assertEquals("/", reqline.getUri());
        Assert.assertEquals(HttpVersion.HTTP_1_1, reqline.getProtocolVersion());
        final Header[] headers = httprequest.getAllHeaders();
        Assert.assertEquals(1, headers.length);
    }

    @Test(expected = MessageConstraintException.class)
    public void testBasicMessageParsingTooManyLeadingEmptyLines() throws Exception {
        final String s =
                "\r\n" +
                "\r\n" +
                "\r\n" +
                "GET / HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(s, StandardCharsets.US_ASCII);

        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser(
                MessageConstraints.custom().setMaxEmptyLineCount(3).build());
        parser.parse(inbuffer);
    }

    @Test
    public void testMessageParsingTimeout() throws Exception {
        final String s =
            "GET \000/ HTTP/1.1\r\000\n" +
            "Host: loca\000lhost\r\n" +
            "User-Agent: whatever\r\n" +
            "Coo\000kie: c1=stuff\r\n" +
            "\000\r\n";
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(
                new TimeoutByteArrayInputStream(s.getBytes(StandardCharsets.US_ASCII)), 16);

        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser();

        int timeoutCount = 0;

        HttpRequest httprequest = null;
        for (int i = 0; i < 10; i++) {
            try {
                httprequest = parser.parse(inbuffer);
                break;
            } catch (final InterruptedIOException ex) {
                timeoutCount++;
            }

        }
        Assert.assertNotNull(httprequest);
        Assert.assertEquals(5, timeoutCount);

        @SuppressWarnings("null") // httprequest cannot be null here
        final RequestLine reqline = httprequest.getRequestLine();
        Assert.assertNotNull(reqline);
        Assert.assertEquals("GET", reqline.getMethod());
        Assert.assertEquals("/", reqline.getUri());
        Assert.assertEquals(HttpVersion.HTTP_1_1, reqline.getProtocolVersion());
        final Header[] headers = httprequest.getAllHeaders();
        Assert.assertEquals(3, headers.length);
    }

    @Test(expected = UnsupportedHttpVersionException.class)
    public void testParsingUnsupportedVersion() throws Exception {
        final String s = "GET / HTTP/2.0\r\n\r\n";
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(s, StandardCharsets.US_ASCII);
        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser();
        parser.parse(inbuffer);
    }

}

