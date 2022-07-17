#!/bin/bash

export KAFKA_PACKAGE="org.apache.spark:spark-sql-kafka-0-10_2.13:3.2.1"
export MAIN_CLASS="it.polimi.middleware.spark.CleaningEnrichment"
export JAR_NAME="./app/middleware_spark_project-1.0.jar"
export SPARK_MASTER="spark://127.0.0.1:7077"
export KAFKA_BOOTSTRAP="127.0.0.1:9092"
export KAFKA_TOPIC="rawInput"
export KAFKA_OUT_TOPIC="richNoise"
export CHECKPOINT_LOCATION="/tmp/cleaning_enrichment/checkpoint"
export NOISEDATA_LOCATION="/tmp/cleaning_enrichment/noise_data"
export MAP_FILE_PATH="./resources/"
export MAP_FILE_NAME="poi_map.json"
export MIN_X=0.0
export MAX_X=60.0
export MIN_Y=0.0
export MAX_Y=80.0
export MIN_VAL=0.0
export MAX_VAL=120.0

../bin/spark-submit \
	--packages $KAFKA_PACKAGE \
	--class $MAIN_CLASS \
	--conf spark.cores.max=1 \
	$JAR_NAME \
	$SPARK_MASTER \
	$KAFKA_BOOTSTRAP \
	$KAFKA_TOPIC \
	$KAFKA_OUT_TOPIC \
	$CHECKPOINT_LOCATION \
	$NOISEDATA_LOCATION \
	$MAP_FILE_PATH \
	$MAP_FILE_NAME \
	$MIN_X \
	$MAX_X \
	$MIN_Y \
	$MAX_Y \
	$MIN_VAL \
	$MAX_VAL
