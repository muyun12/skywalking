/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.core.alarm.provider;

import com.google.gson.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.skywalking.oap.server.core.alarm.AlarmMessage;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import org.junit.*;

public class WebhookCallbackTest implements Servlet {
    private Server server;
    private volatile boolean isSuccess = false;

    @Before
    public void init() throws Exception {
        server = new Server(new InetSocketAddress("127.0.0.1", 8778));
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletContextHandler.setContextPath("/webhook");

        server.setHandler(servletContextHandler);

        ServletHolder servletHolder = new ServletHolder();
        servletHolder.setServlet(this);
        servletContextHandler.addServlet(servletHolder, "/receiveAlarm");

        server.start();
    }

    @After
    public void stop() throws Exception {
        server.stop();
    }

    @Test
    public void testWebhook() {
        Map<String, List<String>> remoteEndpoints = new HashMap<>();
        List<String> remoteEndpoint = new ArrayList<>();
        remoteEndpoint.add("http://127.0.0.1:8778/webhook/receiveAlarm");
        remoteEndpoints.put("default", remoteEndpoint);
        WebhookCallback webhookCallback = new WebhookCallback(remoteEndpoints);
        List<AlarmMessage> alarmMessages = new ArrayList<>(2);
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setScopeId(DefaultScopeDefine.ALL);
        alarmMessage.setAlarmMessage("alarmMessage with [DefaultScopeDefine.All]");
        alarmMessages.add(alarmMessage);
        AlarmMessage anotherAlarmMessage = new AlarmMessage();
        anotherAlarmMessage.setScopeId(DefaultScopeDefine.ENDPOINT);
        anotherAlarmMessage.setAlarmMessage("anotherAlarmMessage with [DefaultScopeDefine.Endpoint]");
        alarmMessages.add(anotherAlarmMessage);
        webhookCallback.doAlarm(alarmMessages);

        Assert.assertTrue(isSuccess);
    }

    @Override public void init(ServletConfig config) throws ServletException {

    }

    @Override public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        if (httpServletRequest.getContentType().equals("application/json")) {
            InputStream inputStream = request.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int readCntOnce;

            while ((readCntOnce = inputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, readCntOnce);
            }

            JsonArray elements = new Gson().fromJson(new String(out.toByteArray()), JsonArray.class);
            if (elements.size() == 2) {
                ((HttpServletResponse)response).setStatus(200);
                isSuccess = true;
                return;
            }

            ((HttpServletResponse)response).setStatus(500);
        }
    }

    @Override public String getServletInfo() {
        return null;
    }

    @Override public void destroy() {

    }

}
