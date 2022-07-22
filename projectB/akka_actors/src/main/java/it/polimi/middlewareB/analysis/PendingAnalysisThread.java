package it.polimi.middlewareB.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.middlewareB.AlwaysSeekToBeginningListener;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class PendingAnalysisThread implements Runnable{
    public PendingAnalysisThread(String kafkaBootstrap){
        this.kafkaBootstrap = kafkaBootstrap;
    }
    @Override
    public void run() {
        Properties config = new Properties();
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "PendingAnalysis");
        config.put("bootstrap.servers", kafkaBootstrap);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //TODO are we sure of this?
        config.setProperty("auto.offset.reset","earliest");


        KafkaConsumer<String, String> pendingConsumer = new KafkaConsumer<>(config);
        KafkaConsumer<String, String> completedConsumer = new KafkaConsumer<>(config);



        ObjectMapper jacksonMapper = new ObjectMapper();
        pendingConsumer.subscribe(List.of("pendingJobs"));
        completedConsumer.subscribe(List.of("completedJobs"));
        int pending = 0;

        while (true) {
            ConsumerRecords<String, String> pendingRecords = pendingConsumer.poll(Duration.ofMillis(1000));
            ConsumerRecords<String, String> completedRecords = completedConsumer.poll(Duration.ofMillis(1000));
            if (pendingRecords.count() != 0){
                //TODO malformed messages are still counted! This may lead to zombies messages that get never deleted
                for (ConsumerRecord record : pendingRecords) {
                    pending++;
                }
            }
            if (completedRecords.count() != 0) {
                for (ConsumerRecord record : completedRecords) {
                    pending--;
                }
            }
            if (pending < 0){
                pending = 0;
            }
            System.out.println("----------------\n" +
                    "Analysis done!\n" +
                    pending + " jobs are pending\n" +
                    "----------------");
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private String kafkaBootstrap;
}
