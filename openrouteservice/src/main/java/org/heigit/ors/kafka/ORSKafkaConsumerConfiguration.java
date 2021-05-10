package org.heigit.ors.kafka;

public class ORSKafkaConsumerConfiguration {
    private final String server;
    private final String topic;
    private final String profile;

    public ORSKafkaConsumerConfiguration(String server, String topic, String profile) {
        this.server = server;
        this.topic = topic;
        this.profile = profile;
    }

    public String getServer() {
        return server;
    }

    public String getTopic() {
        return topic;
    }

    public String getProfile() {
        return profile;
    }
}
