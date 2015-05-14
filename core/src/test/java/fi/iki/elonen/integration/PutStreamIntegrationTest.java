package fi.iki.elonen.integration;

/*
 * #%L
 * NanoHttpd-Core
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.junit.Test;

import fi.iki.elonen.NanoHTTPD;

public class PutStreamIntegrationTest extends IntegrationTestBase<PutStreamIntegrationTest.TestServer> {

    public static class TestServer extends NanoHTTPD {

        public TestServer() {
            super(8192);
        }

        @Override
        public Response serve(IHTTPSession session) {
            Method method = session.getMethod();
            Map<String, String> headers = session.getHeaders();
            int contentLength = Integer.parseInt(headers.get("content-length"));

            byte[] body;
            try {
                DataInputStream dataInputStream = new DataInputStream(session.getInputStream());
                body = new byte[contentLength];
                dataInputStream.readFully(body, 0, contentLength);
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, e.getMessage());
            }

            String response = String.valueOf(method) + ':' + new String(body);
            return newFixedLengthResponse(response);
        }

        @Override
        public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public TestServer createTestServer() {
        return new TestServer();
    }

    @Test
    public void testSimplePutRequest() throws Exception {
        String expected = "This HttpPut request has a content-length of 48.";

        HttpPut httpput = new HttpPut("http://localhost:8192/");
        httpput.setEntity(new ByteArrayEntity(expected.getBytes()));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = this.httpclient.execute(httpput, responseHandler);

        assertEquals("PUT:" + expected, responseBody);
    }
}
