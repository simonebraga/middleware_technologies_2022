#! /bin/sh

./mpi_simulator --db \
		-P 100 \
		-V 10 \
		-W 500 \
		-L 300 \
		--Np 3 \
		--Nv 10 \
		--Dp 2 \
		--Dv 10 \
		--Vp 1 \
		--Vv 14 \
		-t 1
