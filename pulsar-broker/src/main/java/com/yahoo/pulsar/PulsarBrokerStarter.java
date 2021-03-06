/**
 * Copyright 2016 Yahoo Inc.
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
 */
package com.yahoo.pulsar;

import static com.yahoo.pulsar.broker.ServiceConfigurationLoader.create;
import static com.yahoo.pulsar.broker.ServiceConfigurationLoader.isComplete;

import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.yahoo.pulsar.broker.PulsarServerException;
import com.yahoo.pulsar.broker.PulsarService;
import com.yahoo.pulsar.broker.ServiceConfiguration;

public class PulsarBrokerStarter {

    private static ServiceConfiguration loadConfig(String configFile) throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        ServiceConfiguration config = create((new FileInputStream(configFile)));
        // it validates provided configuration is completed
        isComplete(config);
        return config;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Need to specify a configuration file");
        }

        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            log.error("Uncaught exception in thread {}: {}", thread.getName(), exception.getMessage(), exception);
        });

        String configFile = args[0];
        ServiceConfiguration config = loadConfig(configFile);

        @SuppressWarnings("resource")
        final PulsarService service = new PulsarService(config);
        Runtime.getRuntime().addShutdownHook(service.getShutdownService());

        try {
            service.start();
            log.info("PulsarService started");
        } catch (PulsarServerException e) {
            log.error("Failed to start pulsar service.", e);

            Runtime.getRuntime().halt(1);
        }

        service.waitUntilClosed();
    }

    private static final Logger log = LoggerFactory.getLogger(PulsarBrokerStarter.class);
}
