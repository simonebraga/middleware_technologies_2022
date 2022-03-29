#! /bin/sh

#Run the project with the maximum number of hardware threads

mpirun --use-hwthread-cpus mpi_simulator
