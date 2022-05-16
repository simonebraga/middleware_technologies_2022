package it.polimi.middleware.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Demo {

    public static void main(String[] args) {

        // Use default values if not specified otherwise
        // Default values work in a local environment in IntelliJ
        final int x_range = args.length > 0 ? Integer.parseInt(args[0]) : 100;
        final int y_range = args.length > 1 ? Integer.parseInt(args[1]) : 100;
        final int val_range = args.length > 2 ? Integer.parseInt(args[2]) : 100;
        final int sleep_time_ms = args.length > 3 ? Integer.parseInt(args[3]) : 1000;
        final String bootstrap = args.length > 4 ? args[4] : "localhost:9092";
        final String topic = args.length > 5 ? args[5] : "rawNoise";

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //noinspection resource
        final KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // Record format: {"x":11.0,"y":12.0,"val":13.0}
        String toSend;

        System.out.println("[LOG] Demo producer for topic \"" + topic + "\" started with frequency " + 1000.0/sleep_time_ms + " Hz");

        //noinspection InfiniteLoopStatement
        while (true) {

            toSend="{\"x\":" + Math.random()*x_range;
            toSend+=",\"y\":" + Math.random()*y_range;
            toSend+=",\"val\":" + Math.random()*val_range + "}";

            producer.send(new ProducerRecord<>(topic, null, toSend));

            try { TimeUnit.MILLISECONDS.sleep(sleep_time_ms); }
            catch (InterruptedException ignored) {}
        }
    }
}
