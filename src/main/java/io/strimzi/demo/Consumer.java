package io.strimzi.demo;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
// import org.apache.kafka.common.config.SaslConfigs;
// import org.apache.kafka.common.config.SslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Consumer {
    private static Logger LOG = LoggerFactory.getLogger(Consumer.class);

    public static void main(final String[] args) {
        /*
         * Configure the logger
         */
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");

        /*
         * Consumer configuration
         */
        final Map<String, Object> props = new HashMap();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.102.68.38:9094");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
        // props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "EEvAqISFdgEw");
        // props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "./ca.p12");
        // props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        // props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"my-user\" password=\"RcusRrjvimu2\";");


        final KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        /*
         * Consume messages
         */
        consumer.subscribe(Collections.singletonList("my-topic"));
        final ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(30));

        if(records.isEmpty()) {
            LOG.info("No message received :-(");
        } else {
            for (final ConsumerRecord<String, String> record : records)
            {
                LOG.info("Received message: {} / {} (from topic {}, partition {}, offset {})",
                        record.key(),
                        record.value(),
                        record.topic(),
                        record.partition(),
                        record.offset());
            }

            consumer.commitSync();
        }

        /*
         * Close the consumer
         */
        consumer.close();
    }
}
