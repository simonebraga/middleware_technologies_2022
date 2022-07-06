package it.polimi.middlewareB;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;

public class AlwaysSeekToBeginningListener<K, V> implements ConsumerRebalanceListener {
    public AlwaysSeekToBeginningListener(KafkaConsumer consumer){
        this.consumer = consumer;
    }
    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        consumer.seekToBeginning(partitions);
    }

    private KafkaConsumer<K, V> consumer;
}
