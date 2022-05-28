# Introduction

This file contains instructions on how to correctly run the back-end modules (Kafka and Spark) outside of the IntelliJ environment.

## Download packages

As a first step, it is necessary to download the following .zip folders and extract them:

* [Kafka_CBIN](https://polimi365-my.sharepoint.com/:u:/g/personal/10529465_polimi_it/ER5ru0lTFtZMvfoSNM808vUBr8eaz0EC551KSh8e_40E9g?e=POKY2u)
* [Spark_CBIN](https://polimi365-my.sharepoint.com/:u:/g/personal/10529465_polimi_it/EbJIqhkMHhhLvezxg6hG5BEBXYi8SMfXU6sqsF1BE3GCzw?e=9HnzTl)

**NOTE: A working JVM (at least Java 11) is required to run the packages.**

**NOTE: Links to the .zip folders could be outdated with respect to the code in the repository. You must have already downloaded the correct version of Kafka and Spark.**

* [Kafka](https://dlcdn.apache.org/kafka/3.1.0/kafka_2.13-3.1.0.tgz)
* [Spark](https://dlcdn.apache.org/spark/spark-3.2.1/spark-3.2.1-bin-hadoop3.2-scala2.13.tgz)

## Run packages

Both the folders contain useful scripts to run the environment with the correct parameters. The number pre-appended to each script name gives an indication of the order in which the scripts must be run.

**NOTE: Extract each folder in the root of the relative technology (Kafka_CBIN must be in the same location of bin default Kafka folder, same for Spark_CBIN).**

### Kafka

<p align="center">
  <img width=80% src="./resources/kafka_path.png" />
</p>

* `1-run_zookeeper.sh` is necessary for Kafka to work.
* `2-run_kafka.sh` initializes the Kafka environment.
* `3-create_topics.sh` initializes the topics used in the application.
* `4-submit_application.sh` submits the .jar file in the app folder to the Kafka engine. It is possible to run multiple instances of this .jar for scalability.

<br/>

* `D1-run_publisher.sh` is useful for debugging, allows manual publication of events on a specific topic.  
Usage `./D1-run_publisher.sh topic_name`
* `D2-run_subscriber.sh` is useful for debugging, reads the content of a Kafka topic.  
Usage `./D2-run_subscriber.sh topic_name`
* `D3-submit_demo.sh` is useful for debugging, creates random generated records.

<br/>

* `X2-clear_environment.sh` clears the system after the application is closed. Stop Kafka and Zookeeper before running this.

### Spark

<p align="center">
  <img width=80% src="./resources/spark_path.png" />
</p>

* `5-start-local.sh` sets up a local Spark cluster with a master and a worker.
* `6-submit_cleaning_enrichment.sh` submits to the Spark cluster the back-end module for data cleaning and enrichment.
* `7-submit_analysis.sh` submits to the Spark cluster the back-end module for data analysis.

<br/>

* `X1-stop-local.sh` kill the Spark cluster. Stop the Spark streming application before running this.

### Send data

You can act like end-user applications opening a socket connection with the backend with the following command:

`nc localhost 9999`

Then send well-formed strings from the command line.  
(You can open multiple instances if neeeded)
