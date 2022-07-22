CC = mpicc
CFLAGS = -g -lm

.PHONY: all

#IMPORTANT! If you change the name of the program (executable),
#remember to also update the .gitignore file!
PROGRAM = mpi_simulator

all: $(PROGRAM)


$(PROGRAM): main.c
	$(CC) -o $@ $^ $(CFLAGS)
