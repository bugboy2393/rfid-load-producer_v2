package com.walmart.ims;

import com.walmart.ims.events.Store;
import com.walmart.ims.properties.KafkaProperties;
import com.walmart.ims.events.avro.rfid.RfidData;
import com.walmart.ims.events.avro.rfid.RfidEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class LoadProducer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(LoadProducer.class);

    private KafkaProperties kafkaProperties;

    private Store store;
    private RfidEvent rfidEvent;
    private Producer<Store, RfidEvent> producer;
    private int recordsPerSecond;
    private String topic;

    @Autowired
    public LoadProducer(@Qualifier("kafkaProperties") KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        this.store = null;
        this.rfidEvent = RfidEvent.newBuilder()
                .setSpecversion("1.0")
                .setType("rfid.outs.manual.event")
                .setSource("com.walmart.ssaeis.rfid.outs")
                .setId("87a30bdb-3c61-4598-8811-3048b4e64930")
                .setTime("2020-01-06T16:19:08.366Z")
                .setDatacontentype("application/json")
                .setData(RfidData.newBuilder()
                        .setCountry("US")
                        .setStore(4108)
                        .setSession("496813db-5468-4db1-ba46-4838627bf6dc")
                        .setLocation("A_1_1")
                        .setGtin14("08809693686346")
                        .setPrimeItemNumber(580575925)
                        .setDepartment(23)
                        .setReplenishmentGroup("")
                        .build())
                .build();
        this.producer = createProducer(kafkaProperties.getBroker(), false);
        this.recordsPerSecond = kafkaProperties.getRecordsPerSecond();
        this.topic = kafkaProperties.getTopic();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new ProduceRecord(), 0, 1000);
    }

    public <K, V> Producer<K, V> createProducer(String broker, boolean useSchemaRegistry) {
        Properties javaProperties = new Properties();
        javaProperties.put("bootstrap.servers", broker);
        javaProperties.setProperty(ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducerAcks());

        if (useSchemaRegistry) {
            javaProperties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaProperties.getSchemaRegistryURL());
            javaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
            javaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        } else {
            javaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
            javaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        }

        //altering this could change the ordering of records
        javaProperties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, kafkaProperties.getProducerMaxInFlightRequestsPerConnection());
        javaProperties.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        javaProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaProperties.getProducerRequestTimeoutMs());
        //Only retry after one second.
        javaProperties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1_000);
        javaProperties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Long.MAX_VALUE);

        return new KafkaProducer<>(javaProperties);
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LoadProducer.class);
        app.run(args);
    }

    class ProduceRecord extends TimerTask {
        @Override
        public void run() {
            for (int i = 0; i < recordsPerSecond; i++) {
                try {
                    logger.info("Sending event: {} to {}", rfidEvent, topic);
//                    producer.send(new ProducerRecord<>(topic, store, rfidEvent));
                } catch (Exception ex) {
                    logger.error("Exception occurred", ex);
                }
            }
        }
    }
}
