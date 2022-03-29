#include <mpi.h>
#include <stdio.h>

int main(int argc, char *argv[])
{
  int myRank;
  int nProcesses;
  
  MPI_Init(&argc, &argv);
  MPI_Comm_size(MPI_COMM_WORLD, &nProcesses);
  MPI_Comm_rank(MPI_COMM_WORLD, &myRank);

  printf("Hi, I'm process %d out of %d.\n", myRank, nProcesses);

  MPI_Finalize();
  
  return 0;
}
