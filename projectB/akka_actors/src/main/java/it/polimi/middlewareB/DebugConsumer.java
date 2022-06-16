package it.polimi.middlewareB;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class DebugConsumer {
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(new FileOutputStream("/dev/null")));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Properties config = new Properties();
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "single1");
        config.put("bootstrap.servers", "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.setProperty("auto.offset.reset","earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config);

        consumer.subscribe(List.of("pending"));
        //consumer.seek(new TopicPartition("pending"), 0);

        int processedCount = 0;
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            if (records.count() == 0) {

            } else {
                for (ConsumerRecord<String, String> record : records) {
                    System.err.println("record " + processedCount + " is " + record.value());
                    processedCount++;
                }
            }
            System.err.println("Processing done!");
        }


    }
}
