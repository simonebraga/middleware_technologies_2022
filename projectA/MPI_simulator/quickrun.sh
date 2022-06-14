#! /bin/sh

#Run the project with the maximum number of hardware threads

mpirun --use-hwthread-cpus mpi_simulator \
       --db \
       -P 1500 \
       -V 1000 \
       -W 22100 \
       -L 19800 \
       --Np 30 \
       --Nv 70 \
       --Dp 10 \
       --Dv 50 \
       --Vp 1 \
       --Vv 14 \
       -t 1 \
       --origin-latitude 32000 \
       --origin-longitude 2800 \
       --kafka-bridge-address 127.0.0.1

#45.4578090
#9.1737100
