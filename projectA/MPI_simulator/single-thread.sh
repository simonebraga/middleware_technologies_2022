#! /bin/sh

./mpi_simulator --db \
		-P 20 \
		-V 10 \
		-W 20 \
		-L 70 \
		--Np 30 \
		--Nv 70 \
		--Dp 2 \
		--Dv 10 \
		--Vp 1 \
		--Vv 10 \
		-t 10 \
		--origin-latitude  0.0 \
		--origin-longitude 0.0 \
		--kafka-bridge-address \
		127.0.0.1
#		25.29.114.127
		#45.4578090 \
		     #9.1737100 \
