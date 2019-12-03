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

package org.apache.skywalking.apm.agent.core.util;

import org.apache.skywalking.apm.agent.core.boot.AgentPackagePath;
import org.apache.skywalking.apm.agent.core.conf.ConfigNotFoundException;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ignore some exceptions and report success
 * @author muyun12
 */
public class IgnoreExceptionUtil {

    private static final ILog logger = LogManager.getLogger(IgnoreExceptionUtil.class);
    // ignore exceptions config file name
    private static final String CONFIG_FILE_NAME = "/config/ignore_exceptions.config";
    private static final String FAIL_TO_LOAD_CONFIG = "Failed to load " + CONFIG_FILE_NAME + ".";
    public static final List<String> EX_LIST = new ArrayList<String>();

    public static void initialize() {
        logger.info("Start to init ignore exceptions config.");
        BufferedReader bufferedReader = null;
        try {
            File configFile = new File(AgentPackagePath.getPath(), CONFIG_FILE_NAME);
            if (configFile.exists() && configFile.isFile()) {
                logger.info("Ignore Exceptions Config file found in {}.", configFile);
                bufferedReader = new BufferedReader(new FileReader(configFile));
                for (;;) {
                    String ex = bufferedReader.readLine();
                    if (null == ex) {
                        break;
                    }
                    EX_LIST.add(ex);
                }
            } else {
                throw new ConfigNotFoundException(FAIL_TO_LOAD_CONFIG);
            }
            logger.info("Ignore the exceptions below:");
            for (String ex : EX_LIST) {
                logger.info(ex);
            }
        } catch (Exception e) {
            logger.error("Fail to init ignore exception config.", e);
        } finally {
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    logger.error("close buffer reader error.", e);
                }
            }
        }
    }

    private IgnoreExceptionUtil(){
        // prevent instantiation
    }

    public static void main(String[] args) {
        IgnoreExceptionUtil.initialize();
    }
}
