package org.heigit.ors.kafka;

public class ORSKafkaConsumerConfiguration {
    private final String cluster;
    private final String topic;
    private final String profile;
    private final long timeout;

    public ORSKafkaConsumerConfiguration(String cluster, String topic, String profile, long timeout) {
        this.cluster = cluster;
        this.topic = topic;
        this.profile = profile;
        this.timeout = timeout;
    }

    public String getCluster() {
        return cluster;
    }

    public String getTopic() {
        return topic;
    }

    public String getProfile() {
        return profile;
    }

    public long getTimeout() {
        return timeout;
    }

    public boolean hasTimeout() {
        return timeout > 0;
    }
}
