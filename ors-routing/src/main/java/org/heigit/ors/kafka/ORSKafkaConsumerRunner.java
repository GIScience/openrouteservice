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

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class ORSKafkaConsumerRunner implements Runnable {

	// This is a hack necessary until Kafka in ORS is properly done with Spring Boot
	public static volatile boolean hasRunOnce = false;

	private boolean active;
	private final String profile;
	private final Consumer<Long, String> consumer;
	private final long pollTimeout;
	private static final long POLL_TIMEOUT_DEFAULT = 1000;
	private static final Logger LOGGER = Logger.getLogger(ORSKafkaConsumerRunner.class);

	public ORSKafkaConsumerRunner(ORSKafkaConsumerConfiguration config) {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getCluster());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "ORSKafkaConsumer");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumer = new KafkaConsumer<>(props);
		consumer.subscribe(List.of(config.getTopic()));
		this.profile = config.getProfile();
		this.pollTimeout = config.hasTimeout() ? config.getTimeout() : POLL_TIMEOUT_DEFAULT;
		this.active = true;
		LOGGER.debug(String.format("Created Kafka consumer thread listening to %s (%s), passing to %s", config.getCluster(), config.getTopic(), config.getProfile()));
	}

	private void updateProfile(ConsumerRecord<Long, String> consumerRecord) {
        hasRunOnce = true;
        RoutingProfileManager.getInstance().updateProfile(profile, consumerRecord.value());
	}

	public void run() {
		while (active) {
			final ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofMillis(pollTimeout));
			consumerRecords.forEach(this::updateProfile);
			consumer.commitAsync();
		}
		consumer.close();
	}

	public void stop() {
		active = false;
	}
}
