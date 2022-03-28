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

Roughly speaking, the map-reduce paradigm implements the following functionalities:

* **Map** &#8594; Data cleaning and enrinchment

* **Reduce** &#8594; Data analysis

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

This section contains an explanation of what led to the choice of every involved technology, as long as a short summary of the reasoning for which other technologies have been excluded.

### Spark for data processing

The back-end module is a Java application developed using Spark. Since there are no guarantees on the amount of incoming data, it was necessary to build a flexible system capable of handling two extreme cases:

* When a lot of data is incoming.  
Spark is a framework that offers very efficient map-reduce functions to perform optimized calculations on big quantities of data, it is used in the application to create statistics efficiently, even when the amount of incoming values becomes huge.

* When almost no data is received.  
Spark is good since it performs calculations only when there is something in input, not wasting computational performance in an almost "idle" situation.

**Why not Kafka?**

Another alternative we thought about for the back-end was Kafka. The use of Spark would have been anyway necessary for the statistical calculations, and Kafka would end up being used just as a communication interface between the sensors and the back-end. We discarded this option because it'd be a waste to use Kafka just for its communication and storage capabilities.

### Contiki-NG for data collection

The sensors are implemented in C using Contiki-NG and simulated using Cooja, which is able to reproduce the wireless behavior of such devices, which is suitable for the IoT devices' interaction. Moreover, Contiki-NG is able to perform light pre-processing on the collected data with a small performance impact.

The output record of each sensor is structured as follows (depending on the outcome of the pre-processing):

<p align="center">
  <img width=60% src="./resources/record.png" />
</p>

Unlike in our early analysis of the system, it is not necessary to keep the notion of "sensor" over time. The entities of this module are meaningful only until they build the record with the detection. We can then forget about them and accept another read as completely uncorrelated with the previous one. This implies a light implementation also for the "router" nodes, whose only purpose is to redirect the collected data to the back-end.

### MPI for simulation

The simulations are implemented in C using MPI, a specification for high-performance distributed computing scenarios, that has among its main use cases the simulation of population dynamics. The use of MPI allows splitting the workload. Moreover, since the simulation is completely decoupled from the back-end, it can be outsourced to specialized machines with hardware optimized for this type of calculation.

**Why not Akka?**

An alternative we discussed for the simulation was Akka, as we thought of representing each entity capable of producing noise (people, vehicles) as an actor, but as the simulation grows in size, Akka wouldn't be able to keep up with the growing need of computational power. Instead, we opted for MPI which is much faster and needs fewer resources to run (no need for the JVM).

## Main functionalities

_Text goes here_

## Conclusions

_Text goes here_
