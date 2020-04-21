package io.strimzi.demo;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
// import org.apache.kafka.common.config.SaslConfigs;
// import org.apache.kafka.common.config.SslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Producer {
    private static Logger LOG = LoggerFactory.getLogger(Producer.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /*
         * Configure the logger
         */
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");

        /*
         * Producer configuration
         */
        Map<String, Object> props = new HashMap();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.102.68.38:9094");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
        // props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "");
        // props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "");
        // props.put(SaslConfigs.SASL_MECHANISM, "");
        // props.put(SaslConfigs.SASL_JAAS_CONFIG, "");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        /*
         * Produce messages
         */
        ProducerRecord record = new ProducerRecord<String, String>("my-topic", "Hello World");
        RecordMetadata result = (RecordMetadata) producer.send(record).get();
        LOG.info("Message sent (topic {}, partition {}, offset {})",
                result.topic(),
                result.partition(),
                result.offset());

        /*
         * Close the producer
         */
        producer.close();
    }
}
