**4 PAGES DOCUMENTATION**

## Introduction

_Text goes here_

## Architecture

To better exploit the potential of every technology involved, the distributed system is split into three main modules:

* A **data processing module** (back-end), based on Apache Spark, with good processing capabilities to handle a huge amount of incoming data.

* A **data collection module**, based on Contiki-NG, whose purpose is to implement an edge-computing system able to pre-process and redirect the collected data to the back-end.

* A **simulation module**, based on MPI, whose purpose is to hide to the backend module the lack of data when the use of sensors is not allowed.

The system is based on the assumption that communication between modules is never guaranteed since the majority of the devices are intrinsically very unstable (we are talking about IoT devices held by common users). As a consequence, the information records exchanged between the front-end and back-end should be kept as light as possible. For a detailed explanation of how every record is composed, see the section about **design choices**.

### Data processing module (Spark)

_Text goes here_

### Data collection module (Contiki-NG)

_Text goes here_

### Simulation module (MPI)

_Text goes here_

## Design choices

## Main functionalities

## Conclusions
