#!/bin/bash

export KAFKA_OUT_TOPIC="pendingJobs"
export KAFKA_IN_TOPIC="completedJobs"
export KAFKA_BOOTSTRAP_IP=127.0.0.1
export KAFKA_BOOTSTRAP_PORT=9092

../bin/kafka-topics.sh --create --topic $KAFKA_OUT_TOPIC \
	--bootstrap-server $KAFKA_BOOTSTRAP_IP:$KAFKA_BOOTSTRAP_PORT \
	--partitions 30
	
../bin/kafka-topics.sh --create --topic $KAFKA_IN_TOPIC \
	--bootstrap-server $KAFKA_BOOTSTRAP_IP:$KAFKA_BOOTSTRAP_PORT \
	--partitions 30
