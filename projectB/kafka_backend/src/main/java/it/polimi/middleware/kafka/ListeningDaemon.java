package it.polimi.middleware.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ListeningDaemon implements Runnable {

    private final KafkaConsumer<String, String> consumer;
    private final NotificationList notificationList;

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
                    System.out.println("\nJob with ID <" + key + "> completed!\n" +
                            "Check the result folder \"" + value + "\" for the outcome.\n" +
                            "Type \"help\" for the list of commands.\n");
            }
        }
    }
}
