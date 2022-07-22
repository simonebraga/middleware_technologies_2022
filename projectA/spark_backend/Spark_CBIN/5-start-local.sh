#!/bin/bash

# Remember to edit conf/spark-defaults.conf

export SPARK_LOCAL_IP=127.0.0.1
export SPARK_MASTER_HOST=127.0.0.1
export SPARK_MASTER_PORT=7077

mkdir -p /tmp/spark-events

../sbin/start-master.sh
../sbin/start-history-server.sh

../sbin/start-worker.sh spark://$SPARK_MASTER_HOST:$SPARK_MASTER_PORT
