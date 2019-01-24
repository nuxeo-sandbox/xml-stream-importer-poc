/*
 * (C) Copyright 2006-2019 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Contributors:
 *     anechaev
 */
package org.nuxeo.dst.importer.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.runtime.api.Framework;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NotificationServiceImpl implements NotificationService {

    private static final Log log = LogFactory.getLog(NotificationServiceImpl.class);

    public static final String NOTIFICATION_URL = "nuxeo.importer.callback.url";

    protected String url;

    @Override
    public void send(int code, String message) {
        Response response = new Response(code, message);

        String callback = getURL();
        try {
            URL url = new URL(callback);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(HttpMethod.POST.getName());
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            send(response, conn);
        } catch (IOException e) {
            log.error("Could not send a notification to an external API", e);
        }
    }

    protected void send(Response response, HttpURLConnection conn) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (OutputStream os = conn.getOutputStream()) {
            byte[] outStr = mapper.writeValueAsString(response).getBytes();
            conn.setFixedLengthStreamingMode(outStr.length);
            os.write(outStr);
        }
    }

    protected String getURL() {
        if (url == null) {
            url = Framework.getProperty(NOTIFICATION_URL);

            if (url == null) {
                throw new NuxeoException(NOTIFICATION_URL + " is not defined");
            }
        }

        return url;
    }

    public static class Response {

        private int code;

        private String message;

        public Response() {

        }

        public Response(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
