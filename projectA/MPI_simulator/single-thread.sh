#! /bin/sh

./mpi_simulator --db \
		-P 20 \
		-V 10 \
		-W 10 \
		-L 50 \
		--Np 3 \
		--Nv 10 \
		--Dp 2 \
		--Dv 10 \
		--Vp 1 \
		--Vv 5 \
		-t 10 \
		--origin-latitude  45.4578090 \
		--origin-longitude 9.1737100
