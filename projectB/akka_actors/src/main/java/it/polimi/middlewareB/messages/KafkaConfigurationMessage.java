package it.polimi.middlewareB.messages;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

public class KafkaConfigurationMessage {
    public KafkaConfigurationMessage(String bootstrap){
        this.bootstrap = bootstrap;
    }

    public String getBootstrap() { return bootstrap; }

    //private final Properties producerProps;
    private final String bootstrap;
}
