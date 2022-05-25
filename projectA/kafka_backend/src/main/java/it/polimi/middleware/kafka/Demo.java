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
        final double min_x = args.length > 0 ? Double.parseDouble(args[0]) : 0.0;
        final double max_x = args.length > 1 ? Double.parseDouble(args[1]) : 45.0;
        final double min_y = args.length > 2 ? Double.parseDouble(args[2]) : 0.0;
        final double max_y = args.length > 3 ? Double.parseDouble(args[3]) : 35.0;
        final double min_val = args.length > 4 ? Double.parseDouble(args[4]) : 0.0;
        final double max_val = args.length > 5 ? Double.parseDouble(args[5]) : 120.0;
        final int sleep_time_ms = args.length > 6 ? Integer.parseInt(args[6]) : 1000;
        final String bootstrap = args.length > 7 ? args[7] : "localhost:9092";
        final String topic = args.length > 8 ? args[8] : "rawNoise";

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

            toSend="{\"x\":" + (Math.random()*(max_x-min_x)-min_x);
            toSend+=",\"y\":" + (Math.random()*(max_y-min_y)-min_y);
            toSend+=",\"val\":" + (Math.random()*(max_val-min_val)-min_val) + "}";

            producer.send(new ProducerRecord<>(topic, null, toSend));

            try { TimeUnit.MILLISECONDS.sleep(sleep_time_ms); }
            catch (InterruptedException ignored) {}
        }
    }
}
