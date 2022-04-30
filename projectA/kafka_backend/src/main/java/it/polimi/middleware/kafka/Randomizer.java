package it.polimi.middleware.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Randomizer {

    private static Properties getProps(String bootstrapServer) {

        final Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return properties;
    }

    private static String strGen() {
        String result = "{\"x\":" + Math.random()*50 + ",\"y\":" + Math.random()*50 + ",\"val\":[";
        result += Math.random()*50 + ",";
        result += Math.random()*50 + ",";
        result += Math.random()*50 + ",";
        result += Math.random()*50 + ",";
        result += Math.random()*50 + ",";
        result += Math.random()*50 + "]}";
        return result;
    }

    public static void main( String[] args ) {

        // Use default values if not specified otherwise
        // Default values work in a local environment in IntelliJ
        final String bootstrap = args.length > 0 ? args[0] : "localhost:9092";
        final String topic = args.length > 1 ? args[1] : "rawNoise";

        //noinspection resource
        KafkaProducer<String, String> producer = new KafkaProducer<>(getProps(bootstrap));

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignored) {}

            final ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, strGen());
            producer.send(record);
        }
    }
}
