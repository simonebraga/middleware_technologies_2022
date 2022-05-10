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
        final String bootstrap = args.length > 0 ? args[0] : "localhost:9092";
        final String topic = args.length > 1 ? args[1] : "rawNoise";

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //noinspection resource
        final KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // Record format: {"x":11.0,"y":12.0,"val":[13.0,14.0,15.0,16.0,17.0,18.0]}
        String toSend;

        //noinspection InfiniteLoopStatement
        while (true) {

            toSend="{\"x\":" + Math.random()*100;
            toSend+=",\"y\":" + Math.random()*100;
            toSend+=",\"val\":[" + Math.random()*100;
            toSend+="," + Math.random()*100;
            toSend+="," + Math.random()*100;
            toSend+="," + Math.random()*100;
            toSend+="," + Math.random()*100;
            toSend+="," + Math.random()*100 + "]}";

            producer.send(new ProducerRecord<>(topic, null, toSend));

            try { TimeUnit.SECONDS.sleep(1); }
            catch (InterruptedException ignored) {}
        }
    }
}
