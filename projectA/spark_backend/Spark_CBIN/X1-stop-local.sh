#!/bin/bash

../sbin/stop-worker.sh

../sbin/stop-history-server.sh
../sbin/stop-master.sh

rm -rf /tmp/spark-events
rm -rf /tmp/cleaning_enrichment
