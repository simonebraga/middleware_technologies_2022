#! /bin/sh

mpirun --use-hwthread-cpus mpi_simulator --db \
       -P 100 \
       -V 10 \
       -W 10 \
       -L 50 \
       --Np 3 \
       --Nv 10 \
       --Dp 2 \
       --Dv 10 \
       --Vp 1 \
       --Vv 14 \
       -t 1 \
       --origin-latitude  45.4578090 \
       --origin-longitude 9.1737100 \
       --kafka-bridge-address 127.0.0.1
