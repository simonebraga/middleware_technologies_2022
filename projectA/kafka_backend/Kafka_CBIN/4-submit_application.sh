#!/bin/bash

export CLASSPATH="./app/kafka_backend-1.0.jar"
export MAIN_CLASS="it.polimi.middleware.kafka.ConnectionHandler"
export KAFKA_BOOTSTRAP="127.0.0.1:9092"
export KAFKA_TOPIC="rawInput"
export APPLICATION_PORT=9999

../bin/kafka-run-class.sh \
	$MAIN_CLASS \
	$KAFKA_BOOTSTRAP \
	$KAFKA_TOPIC \
	$APPLICATION_PORT
