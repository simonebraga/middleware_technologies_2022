package it.polimi.middleware.kafka;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHandler {

    public static void main(String[] args) throws IOException {

        // Use default values if not specified otherwise
        // Default values work in a local environment in IntelliJ
        final String bootstrap = args.length > 0 ? args[0] : "localhost:9092";
        final String topic = args.length > 1 ? args[1] : "rawNoise";
        final int port = args.length > 2 ? Integer.parseInt(args[2]) : 9999;

        //noinspection resource
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("[LOG] Kafka connection handler listening on port " + port);

        // For each new socket connection, start a producer thread that forwards input strings to the Kafka topic
        //noinspection InfiniteLoopStatement
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Runnable connectionHandler = new ProducerInstance(clientSocket, bootstrap, topic);
            new Thread(connectionHandler).start();
        }
    }
}
