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

package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestErrorResponseProducer {

    private ErrorResponseProducer erp;
    private HttpResponse response;
    private HttpEntity entity;

    @Before
    public void setUp() throws Exception {
        response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        entity = new StringEntity("stuff");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGenerateResponseKeepAlive() {
        erp = new ErrorResponseProducer(response, entity, true);
        final HttpResponse res = erp.generateResponse();

        Assert.assertEquals("keep-alive", res.getFirstHeader(HttpHeaders.CONNECTION).getValue());
        Assert.assertEquals(entity, res.getEntity());
        Assert.assertEquals(200, res.getCode());
    }

    @Test
    public void testGenerateResponseClose() {
        erp = new ErrorResponseProducer(response, entity, false);
        final HttpResponse res = erp.generateResponse();

        Assert.assertEquals("close", res.getFirstHeader(HttpHeaders.CONNECTION).getValue());
        Assert.assertEquals(entity, res.getEntity());
        Assert.assertEquals(200, res.getCode());
    }

}
