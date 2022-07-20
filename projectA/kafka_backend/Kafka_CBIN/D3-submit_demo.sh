#!/bin/bash

export CLASSPATH="./app/kafka_backend-1.0.jar"
export MAIN_CLASS="it.polimi.middleware.kafka.Demo"
export MIN_VAL=0.0
export MAX_VAL=120.0
export MIN_X=0.0
export MAX_X=60.0
export MIN_Y=0.0
export MAX_Y=80.0
export SLEEPTIME=1000 # In milliseconds
export KAFKA_BOOTSTRAP="127.0.0.1:9092"
export KAFKA_TOPIC="rawInput"

../bin/kafka-run-class.sh \
	$MAIN_CLASS \
	$MIN_VAL \
	$MAX_VAL \
	$MIN_X \
	$MAX_X \
	$MIN_Y \
	$MAX_Y \
	$SLEEPTIME \
	$KAFKA_BOOTSTRAP \
	$KAFKA_TOPIC \
	$APPLICATION_PORT
