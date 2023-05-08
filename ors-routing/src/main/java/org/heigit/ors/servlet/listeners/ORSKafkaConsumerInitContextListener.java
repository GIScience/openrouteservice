/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://giscience.uni-hd.de
 *   http://heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. The GIScience licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.heigit.ors.servlet.listeners;

import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigObject;
import org.apache.log4j.Logger;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.kafka.ORSKafkaConsumer;
import org.heigit.ors.kafka.ORSKafkaConsumerConfiguration;
import org.heigit.ors.kafka.ORSKafkaTestCluster;
import org.heigit.ors.routing.RoutingProfileManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;

public class ORSKafkaConsumerInitContextListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ORSKafkaConsumerInitContextListener.class);
    private ORSKafkaConsumer consumer;
    private ORSKafkaTestCluster testCluster;

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        Thread thread = new Thread(() -> {
            while (!RoutingProfileManager.isInitComplete()) { // wait until ORS init is completed
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            if (AppConfig.getGlobal().getBoolean("kafka_test_cluster")) {
                LOGGER.info("Starting Kafka test cluster");
                testCluster = new ORSKafkaTestCluster();
            }
            List<ORSKafkaConsumerConfiguration> configs = loadConfig();
            if (!configs.isEmpty()) {
                LOGGER.info("Starting Kafka consumer");
                consumer = new ORSKafkaConsumer(configs);
                consumer.startConsumer();
            }
        });
        thread.setName("ORS-Kafka-Init");
        thread.start();
    }

    private List<ORSKafkaConsumerConfiguration> loadConfig() {
        List<ORSKafkaConsumerConfiguration> configurations = new ArrayList<>();
        List<? extends ConfigObject> configObjects = AppConfig.getGlobal().getObjectList("kafka_consumer");
        if (!configObjects.isEmpty()) {
            LOGGER.info("Loading Kafka consumer settings");
            for (ConfigObject c : configObjects) {
                try {
                    String cluster = c.toConfig().getString("cluster");
                    String topic = c.toConfig().getString("topic");
                    String profile = c.toConfig().getString("profile");
                    long timeout = c.toConfig().hasPath("timeout") ? c.toConfig().getLong("timeout") : 0;
                    configurations.add(new ORSKafkaConsumerConfiguration(cluster, topic, profile, timeout));
                } catch (ConfigException e) {
                    LOGGER.warn(String.format("Invalid Kafka consumer configuration: %s", e.getMessage()));
                }
            }
        }
        return configurations;
    }

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        if (consumer != null) {
            LOGGER.info("Shutting down Kafka consumer");
            consumer.stopConsumer();
        }
        if (testCluster != null) {
            LOGGER.info("Shutting down Kafka test cluster");
            testCluster.stop();
        }
    }
} 
