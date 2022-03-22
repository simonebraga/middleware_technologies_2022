**4 PAGES DOCUMENTATION**

## Introduction

_Text goes here_

## Architecture

To better exploit the potential of every technology involved, the distributed system is split into three main modules:

* A **data processing module** (back-end), based on Apache Spark, with good processing capabilities to handle a huge amount of incoming data.

* A **data collection module**, based on Contiki-NG, whose purpose is to implement an edge-computing system able to pre-process and redirect the collected data to the back-end.

* A **simulation module**, based on MPI, whose purpose is to hide to the backend module the lack of data when the use of sensors is not allowed.

<p align="center">
  <img width=90% src="./resources/component_diagram.png" />
</p>

The communication between modules is performed through the Internet.

The system is based on the assumption that communication between modules is never guaranteed since the majority of the devices are intrinsically very unstable (we are talking about IoT devices held by common users). As a consequence, the information records exchanged between the front-end and back-end should be kept as light as possible. For a detailed explanation of how every record is composed, see the section about **design choices**.

### Data processing module (Spark)

This module consists of a Spark distributed cluster that analyzes the noise data received by other modules. The data is ingested from TCP sockets, then it's processed by the Spark engine that computes some metrics and stores the result.

### Data collection module (Contiki-NG)

The only purpose of this module is to collect noise data in an energy-efficient way, perform a very light pre-processing and send the data to the back-end module.

* The **IoT end devices**  
1\. Detect the noise data around the sensor.  
2\. Perform a light pre-processing checking the average of the last 6 measurements.  
3\. Send the data to the nearest border router.

* The **router**  
1\. Collects the data from the near IoT devices.  
2\. Forwards the data of the back-end.

### Simulation module (MPI)

The idea behind the simulation module is to hide the possible absence of data to the back-end module, which must focus on the processing of the received data without spending computational resources on something else.

The module must then produce and send data in the **exact** same format produced by the data collection module, getting it from a simulated environment instead of the real one.

## Design choices

In this section, the main design choices are explained for each part of the project architecture and the communication among them.

Sensors

The sensors are implemented in C using Contiki-NG and simulated through the Cooja simulator. In order to simulate Contiki-NG devices, the OS
comes with a simulation tool called Coojam which is able to run Contiki-NG apps on simulated hardware devices. Moreover, the simulation is able to
reproduce the wireless behaviour of such devices which is suitable for the IoT devices interaction.
In this project implementation, every device is simulated through a  COOJA mote, and the proximity is reached by the wireless connectivity offered
by the simulator.

Simulations

The simulations are implemented in C using MPI, a specification used for hihg-performance distributed computed scenarios, that has among its main
use cases the simulation of population dynamics. To do so each agent (person / vehicle) is simulated in its own process, run in parallel to the
others. Similarly, each zone has its own process, which calculates the sum (MPI_SUM) of the noise produced and sends the result to the backend.

Backend
The backend platform is a Java application developed using Spark.
Spark is a framework that offers (among other things) very efficient map-reduce functions to perform optimized calculations on big quantities of data;
using it we can create statistics (such as average noise per week) for each zone very efficiently, even when the amount of values to use becomes huge.

MPI over AKKA
For the simulations we decided to use MPI. An alternative we discussed about was akka, as we thought of representing each person/vehicle as an actor, 
but as the simulation grow in size, akka wouldn't be able to keep up with the growing need of computational power (scale-up would be very limited).
Instead we opted for MPI which is much faster and more optimized toward these kind of implementations (populations dynamics is amoung its use cases in
real life scenarios), and needs less space to run (C instead of Java).

SPARK over KAFKA as backend
Another alternative we thought about was the use of Kafka as the backend for our system: each POI'd be represented as a topic, and it'd contain his average noise level for hour/day/week, and the recordings of the last week. To make up for the lack of raw computational power offered instead by some other technologies, we thought about using some very simple mathematical optimization. In the end, we discarded this option because we thought that it'd be a waste to use kafka just a 'storing mean' in an application of this type, where we don't use any of its strong points, and opted instead for SPARK, much more suited and optimized for operations on big data, and that provides much cleaner calculations (the mathematical tricks we thought about would, in the long run, create a not negligible error).

## Main functionalities

_Text goes here_

## Conclusions

_Text goes here_
