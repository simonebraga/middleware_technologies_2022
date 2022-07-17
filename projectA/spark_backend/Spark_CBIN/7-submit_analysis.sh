#!/bin/bash

export KAFKA_PACKAGE="org.apache.spark:spark-sql-kafka-0-10_2.13:3.2.1"
export MAIN_CLASS="it.polimi.middleware.spark.Analysis"
export JAR_NAME="./app/middleware_spark_project-1.0.jar"
export SPARK_MASTER="spark://127.0.0.1:7077"
export NOISEDATA_LOCATION="/tmp/cleaning_enrichment/noise_data"
export CHECKPOINT_LOCATION="/tmp/analysis/checkpoint"
export POI_AMOUNT=18
export Q3_THRESHOLD=70.0

mkdir /tmp
mkdir /tmp/cleaning_enrichment
mkdir /tmp/cleaning_enrichment/noise_data
mkdir /tmp/cleaning_enrichment/noise_data/default
cp resources/default.csv /tmp/cleaning_enrichment/noise_data/default/default.csv

../bin/spark-submit \
	--packages $KAFKA_PACKAGE \
	--class $MAIN_CLASS \
	--conf spark.cores.max=1 \
	$JAR_NAME \
	$SPARK_MASTER \
	$NOISEDATA_LOCATION \
	$CHECKPOINT_LOCATION \
	$POI_AMOUNT \
	$Q3_THRESHOLD
