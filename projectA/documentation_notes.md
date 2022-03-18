This document contains notions that shall not be part of the documentation but are very good topics for the presentation. It also contains some notes on what should be put inside the final documentation.

## Introduction

## Architecture

### Data processing module (Spark)

The fundamental step of back-end processing is associating each noise record with the nearest POI (Point Of Interest). It's a computationally expensive task and only on the back-end there is a map with the POIs; for this reason, the task is performed by the Spark cluster.

The process of associating each noise record with the nearest POI can be schematized as follows:

<p align="center">
  <img width=75% src="./resources/spark_process.png" />
</p>

### Data collection module (Contiki-NG)

### Simulation module (MPI)

The simulated environment looks as follows:

<p align="center">
  <img width=60% src="./resources/simulation_module.png" />
</p>

Every dot of the grid is a sensor, in some cases located in the "noise range" of one or more entities (humans or vehicles). The position of every entity and the noise detected by every sensor are recomputed every given time step.

For simplicity
* The "noise range" is a squared surface.
* Every entity is in straight motion towards a cardinal point, with a predefined probability of changing direction at every time step.
* If an entity reaches the region's border, it inverts its direction.

The steps of the simulation are
1. The set of entities is shared among all the cores. Each core processes a subset of the total amount of entities.
2. Every core computes which are the sensors covered by the noise range of each entity of its competence.
3. Every core builds a 2D map of the sensors, associating to each one the value of detected noise.
4. The maps are reduced to a single map using the sum operator. The result is a map with the total amount of noise detected by every sensor.

The following picture schematizes the process:

<p align="center">
  <img width=75% src="./resources/simulation_process.png" />
</p>

The last important thing is that the data produced by the simulation is of the **exact** same format as the data produced by the Contiki module. This has two main advantages:
1. The Spark module doesn't need to handle differently the simulations and the real world
2. If the simulation is outsourced, the Spark module can use all its computational power to handle and process the incoming data
3. The simulation can be performed on a stand-alone module with specialized hardware for MPI

## Design choices

Why **Spark**?  
The system must be able to handle two extreme cases:
* When a lot of data is incoming
* When almost no data is given

Spark is good since it performs calculation only when there is something in input, and yet it is very good in handling a huge amount of data.

**Contiki + Spark** instead of **Akka**:  
Unlike in our early analysis of the system, it is not necessary to keep the notion of "actor" over time. The entities of the data collection module are meaningful only until they build the record with noise detection and coordinates. We can then forget them and accept another read as completely uncorrelated with the previous one (I don't care if two near reads are from different entities or not).

Why **MPI**?  
It's the best way to perform a simulation of moving people since we don't need the notion of "entity that moves around"; instead we only need the noise produced by those entities.
Moreover, the choice of MPI over Node-RED is given by the different computational capacities of the two. The pre-processing of the data that needs to be performed is very basic, so the implementation in Node-RED on IoT devices would have led to higher (useless) power consumption and the need to build the behavior of the single device instead of the system as a whole big entity.

## Main functionalities

## Conclusions
