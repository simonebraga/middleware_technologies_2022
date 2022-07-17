#!/bin/bash

export CLASSPATH="./app/kafka_backend-1.0.jar"
export MAIN_CLASS="it.polimi.middleware.kafka.App"
export KAFKA_BOOTSTRAP="127.0.0.1:9092"
export KAFKA_OUT_TOPIC="pendingJobs"
export KAFKA_IN_TOPIC="completedJobs"
export JOB_FILE_PATH="./resources/"
export JOB_FILE_NAME="job_list.json"
export KEY_LENGTH=8
export DEBUG_MODE=true

../bin/kafka-run-class.sh \
	$MAIN_CLASS \
	$KAFKA_BOOTSTRAP \
	$KAFKA_OUT_TOPIC \
	$KAFKA_IN_TOPIC \
	$JOB_FILE_PATH \
	$JOB_FILE_NAME \
	$KEY_LENGTH \
	$DEBUG_MODE
