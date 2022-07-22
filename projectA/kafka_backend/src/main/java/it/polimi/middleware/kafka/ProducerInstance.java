package it.polimi.middleware.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class ProducerInstance implements Runnable {

    private final Socket clientSocket;
    private final Scanner in;
    private final String topic;
    private final KafkaProducer<String, String> producer;

    public ProducerInstance(Socket clientSocket, String bootstrap, String topic) throws IOException {

        this.clientSocket = clientSocket;
        this.in = new Scanner(clientSocket.getInputStream());
        this.topic = topic;

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(properties);
    }

    // Loop on the input stream of the socket connection until it closes
    @Override
    public void run() {
        System.out.println("[LOG] New connection on port " + clientSocket.getPort());
        try {
            //noinspection InfiniteLoopStatement
            while (true) producer.send(new ProducerRecord<>(topic, null, in.nextLine()));
        } catch (Exception e) {
            System.out.println("[LOG] Connection closed on port " + clientSocket.getPort());
        }
    }
}
