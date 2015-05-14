package fi.iki.elonen;

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

import static junit.framework.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class HttpPostRequestTest extends HttpServerTest {

    public static final String CONTENT_LENGTH = "Content-Length: ";

    public static final String FIELD = "caption";

    public static final String VALUE = "Summer vacation";

    public static final String FIELD2 = "location";

    public static final String VALUE2 = "Grand Canyon";

    public static final String POST_RAW_CONTENT_FILE_ENTRY = "postData";

    public static final String VALUE_TEST_SIMPLE_RAW_DATA_WITH_AMPHASIS = "Test raw data & Result value";

    /**
     * contains common preparation steps for testing POST with Multipart Form
     *
     * @param fileName
     *            Name of file to be uploaded
     * @param fileContent
     *            Content of file to be uploaded
     * @return input String with POST request complete information including
     *         header, length and content
     */
    private String preparePostWithMultipartForm(String fileName, String fileContent) {
        String divider = UUID.randomUUID().toString();
        String header = "POST " + HttpServerTest.URI + " HTTP/1.1\nContent-Type: " + "multipart/form-data, boundary=" + divider + "\n";
        String content =
                "--" + divider + "\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD + "\"; filename=\"" + fileName + "\"\n"
                        + "Content-Type: image/jpeg\r\n" + "\r\n" + fileContent + "\r\n" + "--" + divider + "--\n";
        int size = content.length() + header.length();
        int contentLengthHeaderValueSize = String.valueOf(size).length();
        int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
        String input = header + HttpPostRequestTest.CONTENT_LENGTH + (contentLength + 5) + "\r\n\r\n" + content;

        return input;
    }

    @Test
    public void testPostWithMultipartFormUpload() throws Exception {
        String filename = "GrandCanyon.txt";
        String fileContent = HttpPostRequestTest.VALUE;
        String input = preparePostWithMultipartForm(filename, fileContent);

        invokeServer(input);

        assertEquals(1, this.testServer.parms.size());
        BufferedReader reader = new BufferedReader(new FileReader(this.testServer.files.get(HttpPostRequestTest.FIELD)));
        List<String> lines = readLinesFromFile(reader);
        assertLinesOfText(new String[]{
            fileContent
        }, lines);
    }

    @Test
    public void testPostWithMultipartFormUploadFilenameHasSpaces() throws Exception {
        String fileNameWithSpace = "Grand Canyon.txt";
        String fileContent = HttpPostRequestTest.VALUE;
        String input = preparePostWithMultipartForm(fileNameWithSpace, fileContent);

        invokeServer(input);

        String fileNameAfter = new ArrayList<String>(this.testServer.parms.values()).get(0);

        assertEquals(fileNameWithSpace, fileNameAfter);
    }

    @Test
    public void testPostWithMultipleMultipartFormFields() throws Exception {
        String divider = UUID.randomUUID().toString();
        String header = "POST " + HttpServerTest.URI + " HTTP/1.1\nContent-Type: " + "multipart/form-data; boundary=" + divider + "\n";
        String content =
                "--" + divider + "\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD + "\"\n" + "\n" + HttpPostRequestTest.VALUE + "\n" + "--"
                        + divider + "\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD2 + "\"\n" + "\n" + HttpPostRequestTest.VALUE2 + "\n" + "--"
                        + divider + "--\n";
        int size = content.length() + header.length();
        int contentLengthHeaderValueSize = String.valueOf(size).length();
        int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
        String input = header + HttpPostRequestTest.CONTENT_LENGTH + (contentLength + 4) + "\r\n\r\n" + content;
        invokeServer(input);

        assertEquals(2, this.testServer.parms.size());
        assertEquals(HttpPostRequestTest.VALUE, this.testServer.parms.get(HttpPostRequestTest.FIELD));
        assertEquals(HttpPostRequestTest.VALUE2, this.testServer.parms.get(HttpPostRequestTest.FIELD2));
    }

    @Test
    public void testPostWithMultipleMultipartFormFieldsWhereContentTypeWasSeparatedByComma() throws Exception {
        String divider = UUID.randomUUID().toString();
        String header = "POST " + HttpServerTest.URI + " HTTP/1.1\nContent-Type: " + "multipart/form-data, boundary=" + divider + "\n";
        String content =
                "--" + divider + "\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD + "\"\n" + "\n" + HttpPostRequestTest.VALUE + "\n" + "--"
                        + divider + "\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD2 + "\"\n" + "\n" + HttpPostRequestTest.VALUE2 + "\n" + "--"
                        + divider + "--\n";
        int size = content.length() + header.length();
        int contentLengthHeaderValueSize = String.valueOf(size).length();
        int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
        String input = header + HttpPostRequestTest.CONTENT_LENGTH + (contentLength + 4) + "\r\n\r\n" + content;
        invokeServer(input);

        assertEquals(2, this.testServer.parms.size());
        assertEquals(HttpPostRequestTest.VALUE, this.testServer.parms.get(HttpPostRequestTest.FIELD));
        assertEquals(HttpPostRequestTest.VALUE2, this.testServer.parms.get(HttpPostRequestTest.FIELD2));
    }

    @Test
    public void testSimplePostWithSingleMultipartFormField() throws Exception {
        String divider = UUID.randomUUID().toString();
        String header = "POST " + HttpServerTest.URI + " HTTP/1.1\nContent-Type: " + "multipart/form-data; boundary=" + divider + "\n";
        String content =
                "--" + divider + "\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD + "\"\n" + "\n" + HttpPostRequestTest.VALUE + "\n" + "--"
                        + divider + "--\n";
        int size = content.length() + header.length();
        int contentLengthHeaderValueSize = String.valueOf(size).length();
        int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
        String input = header + HttpPostRequestTest.CONTENT_LENGTH + (contentLength + 4) + "\r\n\r\n" + content;
        invokeServer(input);

        assertEquals(1, this.testServer.parms.size());
        assertEquals(HttpPostRequestTest.VALUE, this.testServer.parms.get(HttpPostRequestTest.FIELD));
    }

    @Test
    public void testSimpleRawPostData() throws Exception {
        String header = "POST " + HttpServerTest.URI + " HTTP/1.1\n";
        String content = HttpPostRequestTest.VALUE_TEST_SIMPLE_RAW_DATA_WITH_AMPHASIS + "\n";
        int size = content.length() + header.length();
        int contentLengthHeaderValueSize = String.valueOf(size).length();
        int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
        String input = header + HttpPostRequestTest.CONTENT_LENGTH + (contentLength + 4) + "\r\n\r\n" + content;
        invokeServer(input);
        assertEquals(0, this.testServer.parms.size());
        assertEquals(1, this.testServer.files.size());
        assertEquals(HttpPostRequestTest.VALUE_TEST_SIMPLE_RAW_DATA_WITH_AMPHASIS, this.testServer.files.get(HttpPostRequestTest.POST_RAW_CONTENT_FILE_ENTRY));
    }

}
