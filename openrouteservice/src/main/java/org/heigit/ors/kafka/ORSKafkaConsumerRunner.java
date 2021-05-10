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
package org.heigit.ors.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.RoutingProfileManager;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ORSKafkaConsumerRunner implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ORSKafkaConsumerRunner.class);
    private final String profile;
    private boolean active;
    private final Consumer<Long, String> consumer;
    private static final long POLL_TIMEOUT = 1000;

    public ORSKafkaConsumerRunner(ORSKafkaConsumerConfiguration settings) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.getServer());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ORSKafkaConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(settings.getTopic()));
        this.profile = settings.getProfile();
        this.active = true;
    }

    private void updateProfile(ConsumerRecord<Long, String> r) {
        try {
            RoutingProfileManager.getInstance().updateProfile(profile, r.value());
        } catch (IOException e) {
            LOGGER.error("ORS has not been initialized");
            this.active = false;
        }
    }

    public void run() {
        while (active) {
            LOGGER.info("kafka consumer running, target: " + profile);
            try {
//                final ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofMillis(POLL_TIMEOUT));
//                consumerRecords.forEach(this::updateProfile);
//                consumer.commitAsync();
                Thread.sleep(POLL_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        consumer.close();
    }

    public void stop() {
        active = false;
    }
}
