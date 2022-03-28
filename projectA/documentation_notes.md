This document contains notions that shall not be part of the documentation but are very good topics for the presentation. It also contains some notes on what should be put inside the final documentation.

## Architecture

### Data processing module (Spark)

The fundamental step of back-end processing is associating each noise record with the nearest POI (Point Of Interest). It's a computationally expensive task and only on the back-end there is a map with the POIs; for this reason, the task is performed by the Spark cluster.

The process of associating each noise record with the nearest POI can be schematized as follows:

<p align="center">
  <img width=75% src="./resources/spark_process.png" />
</p>


The Spark cluster is composed of two main steps: map and reduce.

#### Map

We get as input a set of tuples in the form `<position, noise value>`.
It roughly correspond to the "Data cleaning and enrichment" section of the specification. We:
* clean the data (discarding invalid data, i.e. below zero)
* associate each measurement with its nearest POI, using the position.

We so transform data in the form `<POI, noise value>`. The POI is the reduction key.

#### Reduce

It roughly corresponds to the "Data analysis" section of the specification. Spark handles the shuffling: assuming we are acting on the set of measurement for a single POI, we:
* Compute the relevant averages (hourly, daily, and weekly)
* Compute the streak duration
* Keep the top 10

These tasks can be performed using [Spark structured streaming](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html). Structured streaming seems to be more powerful than regular Spark straming.

Spark structured streaming naturally tags each event with a timestamp. Data coming from sensors can be nicely decorated with *event time* (time attached to the source). In this way, we can access the timestamp of the noise level mesurement, and this time is preserved in case of congestion: if the measurement is delivered late, the timestamp will still be correctly recognized and processed. However, **it is still open** (effectively as a TODO) how can we exploit this with the simulation data (since there isn't a straightforward correspondance with "clock time").
  
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