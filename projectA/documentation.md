**4 PAGES DOCUMENTATION**

## Introduction

_Text goes here_

## Architecture

To better exploit the potential of every technology involved, the distributed system is split into three main modules:

* A **data processing module** (back-end), based on Apache Spark, with good processing capabilities to handle a huge amount of incoming data.

* A **data collection module**, based on Contiki-NG, whose purpose is to implement an edge-computing system able to pre-process and redirect the collected data to the back-end.

* A **simulation module**, based on MPI, whose purpose is to hide to the backend module the lack of data when the use of sensors is not allowed.

//TODO Put picture here

The system is based on the assumption that communication between modules is never guaranteed since the majority of the devices are intrinsically very unstable (we are talking about IoT devices held by common users). As a consequence, the information records exchanged between the front-end and back-end should be kept as light as possible. For a detailed explanation of how every record is composed, see the section about **design choices**.

### Data processing module (Spark)

_Text goes here_

### Data collection module (Contiki-NG)

_Text goes here_

### Simulation module (MPI)

The idea behind the simulation module is to hide the possible absence of data to the back-end module, which must focus on the processing of the received data without spending computational resources on something else.

The module must then produce and send data in the **exact** same format produced by the data collection module, getting it from a simulated environment instead of the real one.

The simulated environment looks as follows:

//TODO Put picture here

Every dot of the grid is a sensor, in some cases located in the "noise range" of one or more entities (humans or vehicles). The position of every entity and the noise detected by every sensor are recomputed every given time step.

For simplicity
* The "noise range" is a squared surface.
* Every entity is in straight motion towards a cardinal point, with a predefined probability of changing direction at every time step.
* If an entity reaches the region's border, it inverts its direction.

## Design choices

_Text goes here_

## Main functionalities

_Text goes here_

## Conclusions

_Text goes here_
