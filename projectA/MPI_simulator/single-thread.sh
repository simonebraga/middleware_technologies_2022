#! /bin/sh

./mpi_simulator --db \
		-P 20 \
		-V 10 \
		-W 10 \
		-L 50 \
		--Np 30 \
		--Nv 70 \
		--Dp 2 \
		--Dv 10 \
		--Vp 1 \
		--Vv 10 \
		-t 10 \
		--origin-latitude  45.4578090 \
		--origin-longitude 9.1737100 \
		--kafka-bridge-address \
		# 25.29.114.127
		127.0.0.1
