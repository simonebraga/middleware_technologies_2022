#!/bin/bash

export KAFKA_BOOTSTRAP_IP=127.0.0.1
export KAFKA_BOOTSTRAP_PORT=9092

if [ $# -eq 0 ]
then
	echo "Usage: $0 topic_name"
	echo ""
	echo "The following topics are available:"
	../bin/kafka-topics.sh --list --bootstrap-server $KAFKA_BOOTSTRAP_IP:$KAFKA_BOOTSTRAP_PORT
	exit 1
fi

../bin/kafka-console-consumer.sh --topic $1 --from-beginning --bootstrap-server $KAFKA_BOOTSTRAP_IP:$KAFKA_BOOTSTRAP_PORT
