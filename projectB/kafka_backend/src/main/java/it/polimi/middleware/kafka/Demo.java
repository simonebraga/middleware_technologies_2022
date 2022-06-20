package it.polimi.middleware.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Demo implements Runnable {

    private final String topic_out;
    private final String topic_in;
    private final KafkaConsumer<String, String> consumer;
    private final KafkaProducer<String, String> producer;

    public Demo(String topic_out, String topic_in, String bootstrap) {

        this.topic_out = topic_out;
        this.topic_in = topic_in;

        final Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "debug");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        this.consumer = new KafkaConsumer<>(consumerProps);

        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(producerProps);

        System.out.println("[LOG] Debug thread initialized");
    }

    @Override
    public void run() {

        consumer.subscribe(Collections.singletonList(topic_out));

        System.out.println("[LOG] Debug thread started");

        //noinspection InfiniteLoopStatement
        while (true) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(10, ChronoUnit.SECONDS));
            for (final ConsumerRecord<String, String> record : records) {
                try { TimeUnit.SECONDS.sleep(10); }
                catch (InterruptedException ignored) {}
                producer.send(new ProducerRecord<>(topic_in, record.key(), "/result/folder"));
            }
        }
    }
}
