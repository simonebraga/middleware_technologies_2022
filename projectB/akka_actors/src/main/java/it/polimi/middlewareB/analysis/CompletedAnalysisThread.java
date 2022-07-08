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

public class CompletedAnalysisThread implements Runnable{
    public CompletedAnalysisThread(String kafkaBootstrap){
        this.kafkaBootstrap = kafkaBootstrap;
    }

    @Override
    public void run() {
        Properties config = new Properties();
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "CompletedAnalysis");
        config.put("bootstrap.servers", kafkaBootstrap);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //TODO are we sure of this?
        config.setProperty("auto.offset.reset","earliest");


        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config);



        ObjectMapper jacksonMapper = new ObjectMapper();
        consumer.subscribe(List.of("completedJobs"));

        while(true) {


            int debugCompletedInMinute = 0;
            int completedInMonth = 0;
            int completedInWeek = 0;
            int completedInDay = 0;
            int completedInHour = 0;

            LocalDateTime now = LocalDateTime.now();
            //TODO is a poll needed also before the seekToBeginning()?
            // Note how, at the start of the application, the results may be 0
            consumer.seekToBeginning(consumer.assignment());
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            if (records.count() != 0) {
                for (ConsumerRecord<String, String> record : records) {
                    LocalDateTime timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(record.timestamp()),
                            TimeZone.getDefault().toZoneId());

                    if (ChronoUnit.MONTHS.between(timestamp, now) < 1) {
                        completedInMonth++;
                    }
                    if (ChronoUnit.WEEKS.between(timestamp, now) < 1){
                        completedInWeek++;
                    }
                    if (ChronoUnit.DAYS.between(timestamp, now) < 1){
                        completedInDay++;
                    }
                    if (ChronoUnit.HOURS.between(timestamp, now) < 1){
                        completedInHour++;
                    }
                    if (ChronoUnit.MINUTES.between(timestamp, now) < 1){
                        debugCompletedInMinute++;
                    }
                }
            }
            System.out.println("----------------\n" +
                    "Analysis done!\n" + //TODO remove the debug analysis
                    "DEBUG: " + debugCompletedInMinute + " jobs completed in last minute\n" +
                    completedInHour + " jobs completed in last hour\n" +
                    completedInDay + " jobs completed in last day\n" +
                    completedInWeek + " jobs completed in last week\n" +
                    completedInMonth + " jobs completed in last month\n" +
                    "----------------");


            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final String kafkaBootstrap;
}
