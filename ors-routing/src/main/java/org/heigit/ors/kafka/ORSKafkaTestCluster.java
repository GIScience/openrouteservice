package org.heigit.ors.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Time;
import org.apache.log4j.Logger;
import org.springframework.util.FileSystemUtils;
import scala.Option;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;
import java.util.Set;

public class ORSKafkaTestCluster {
    private TestingServer zookeeper;
    private ORSKafkaProducerRunner producer;
    private KafkaServer kafkaServer;
    private File kafkaTmpLogsDir;
    private static final Logger LOGGER = Logger.getLogger(ORSKafkaTestCluster.class);

    public ORSKafkaTestCluster() {
        try {
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
            kafkaTmpLogsDir = Files.createTempDirectory("ORSKafkaTemp", attr).toFile();
            zookeeper = new TestingServer(true);
            LOGGER.debug("Starting zookeeper server on port: " + zookeeper.getPort());
            Properties props = new Properties();
            props.put("zookeeper.connect", zookeeper.getConnectString());
            props.put("port", "9092");
            props.put("broker.id", "0");
            props.put("log.dirs", kafkaTmpLogsDir.getAbsolutePath());
            props.put("offsets.topic.replication.factor", "1");
            KafkaConfig kafkaConfig = new KafkaConfig(props);
            Option<String> threadNamePrefix = Option.apply("kafka-server");
            kafkaServer = new KafkaServer(kafkaConfig, Time.SYSTEM, threadNamePrefix, true);
            kafkaServer.startup();
            producer = new ORSKafkaProducerRunner("127.0.0.1:9092");
            new Thread(producer).start();
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public void stop() {
        try {
            producer.stop();
            kafkaServer.shutdown();
            kafkaServer.awaitShutdown();
            zookeeper.close();
            FileSystemUtils.deleteRecursively(kafkaTmpLogsDir);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    private static class ORSKafkaProducerRunner implements Runnable {
        private final KafkaProducer<Long, String> producer;
        private boolean active;
        private static final long PRODUCER_INTERVAL = 3000;

        public ORSKafkaProducerRunner(String connectionString) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, connectionString);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producer = new KafkaProducer<>(props);
            active = true;
        }

        public void run() {
            long index = 1;
            ObjectMapper mapper = new ObjectMapper();
            while (active) {
                try {
                    ORSKafkaConsumerMessageSpeedUpdate messageSpeedUpdate = ORSKafkaConsumerMessageSpeedUpdate.generateRandom();
                    producer.send(new ProducerRecord<>("test-topic", index, mapper.writeValueAsString(messageSpeedUpdate)));
                    index++;
                    Thread.sleep(PRODUCER_INTERVAL);
                } catch (JsonProcessingException e) {
                    // should not happen
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void stop() {
            active = false;
        }
    }

}
