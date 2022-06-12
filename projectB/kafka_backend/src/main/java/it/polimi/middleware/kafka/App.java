package it.polimi.middleware.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Scanner;

public class App {

    // This method generates a random string that is used as key for the jobs
    private static String keyGen(int keyLength) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < keyLength; i++) {
            int rand = (int) (Math.random() * 62);
            if (rand <= 9) rand+=48;
            else if (rand <= 35) rand+=55;
            else rand+=61;
            output.append((char) rand);
        } return output.toString();
    }

    public static void main( String[] args ) {

        // Use default values if not specified otherwise
        // Default values work in a local environment in IntelliJ
        final String bootstrap = args.length > 0 ? args[0] : "localhost:9092";
        final String topic_out = args.length > 1 ? args[1] : "pendingJobs";
        final String topic_in = args.length > 2 ? args[2] : "completedJobs";
        final int keyLength = args.length > 3 ? Integer.parseInt(args[3]) : 8;
        final boolean debugMode = args.length <= 4 || Boolean.parseBoolean(args[4]);

        if (debugMode) {
            Runnable demo = new Demo(topic_out, topic_in, bootstrap);
            new Thread(demo).start();
        }

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        final KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        final Scanner in = new Scanner(System.in);

        System.out.println("[LOG] Application started");

        //noinspection InfiniteLoopStatement
        while (true)
            producer.send(new ProducerRecord<>(topic_out, keyGen(keyLength), "[PAYLOAD] " + in.nextLine()));
    }
}
