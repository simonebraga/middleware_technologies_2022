# Compute Infrastructure

## Introduction

This project aims to design and implement a system that accepts compute tasks from clients and executes them on a pool of processes. Each request from a client includes the name of the task to be executed (e.g. image compression), a payload with the parameters to be used in the computation (e.g. a set of images and a compression ratio), and the name of a directory where to store results.

## Architecture

The distributed system is split into two modules:

* A **task aggregation module** based on Apache Kafka.

* A **task performance** module based on Akka.

## Design choices

## Main functionalities

## Conclusions