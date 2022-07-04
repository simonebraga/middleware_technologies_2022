package it.polimi.middleware.kafka.utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ListeningDaemon implements Runnable {

    private final KafkaConsumer<String, String> consumer;
    private final NotificationList notificationList;

    // Returns null if key is not found
    // Pass a consumer with new randomized ID each time this function is called
    public static String retroactiveCheck(KafkaConsumer<String, String> retroactiveConsumer, String key) {
        final ConsumerRecords<String, String> records = retroactiveConsumer.poll(Duration.of(1, ChronoUnit.SECONDS));
        for (final ConsumerRecord<String, String> record : records)
            if (record.key().equals(key))
                return record.value();
        return null;
    }

    public ListeningDaemon(KafkaConsumer<String, String> consumer, NotificationList notificationList) {
        this.consumer = consumer;
        this.notificationList = notificationList;
    }

    @Override
    public void run() {

        String key; String value;

        //noinspection InfiniteLoopStatement
        while (true) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(10, ChronoUnit.SECONDS));
            for (final ConsumerRecord<String, String> record : records) {
                key = record.key(); value = record.value();
                if (notificationList.checkRemove(key))
                    //System.out.println("\nJob with ID <" + key + "> completed!\n" +
                    //       "Check the result folder \"" + value + "\" for the outcome.\n" +
                    //       "Type \"help\" for the list of commands.\n");
                    System.out.println("Job with ID <" + key + "> completed!\n" +
                            value);
            }
        }
    }
}
