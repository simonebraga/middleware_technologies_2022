#!/bin/bash

../sbin/stop-worker.sh

../sbin/stop-history-server.sh
../sbin/stop-master.sh

rm -rf /tmp/spark-events
rm -rf /tmp/checkpoint
rm -rf /tmp/noise_data
