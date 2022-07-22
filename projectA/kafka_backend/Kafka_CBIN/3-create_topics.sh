#!/bin/bash

export KAFKA_TOPIC="rawInput"
export KAFKA_OUT_TOPIC="richNoise"
export KAFKA_BOOTSTRAP_IP=127.0.0.1
export KAFKA_BOOTSTRAP_PORT=9092
export KAFKA_RETENTION=300000 # Retention time in ms

../bin/kafka-topics.sh --create --topic $KAFKA_TOPIC \
	--bootstrap-server $KAFKA_BOOTSTRAP_IP:$KAFKA_BOOTSTRAP_PORT \
	--config retention.ms=$KAFKA_RETENTION
	
../bin/kafka-topics.sh --create --topic $KAFKA_OUT_TOPIC \
	--bootstrap-server $KAFKA_BOOTSTRAP_IP:$KAFKA_BOOTSTRAP_PORT \
	--config retention.ms=$KAFKA_RETENTION
