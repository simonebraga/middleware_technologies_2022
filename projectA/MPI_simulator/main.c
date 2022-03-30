#include <getopt.h>
#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

static int debugFlag = 0;

int main(int argc, char *argv[]) {
  int myRank;
  int nProcesses;

  // simulation parameters
  // NOTE: THEY ARE SIMPLE INTS FOR NOW!
  int P, V, W, L, Np, Nv, Dp, Dv, Vp, Vv, t;

  static const struct option longOptions[] = {
      // debug option
      {"debug", no_argument, &debugFlag, 1},
      {"db", no_argument, &debugFlag, 1},

      {"n-of-people", required_argument, NULL, 'P'},

      {"n-of-vehicles", required_argument, NULL, 'V'},

      {"width-of-region", required_argument, NULL, 'W'},

      {"length-of-region", required_argument, NULL, 'L'},

      {"noise-per-person", required_argument, NULL, 'n'},
      {"Np", required_argument, NULL, 'n'}, // alias of precedent

      {"noise-per-vehicle", required_argument, NULL, 'N'},
      {"Nv", required_argument, NULL, 'N'}, // alias of precedent

      {"radius-of-person", required_argument, NULL, 'd'},
      {"Dp", required_argument, NULL, 'd'}, // alias

      {"radius-of-vehicle", required_argument, NULL, 'D'},
      {"Dv", required_argument, NULL, 'D'}, // alias

      {"speed-of-person", required_argument, NULL, 'u'},
      {"Vp", required_argument, NULL, 'u'}, // alias

      {"speed-of-vehicle", required_argument, NULL, 'U'},
      {"Vv", required_argument, NULL, 'U'}, // alias

      {"time-step", required_argument, NULL, 't'},
      {0, 0, 0, 0}};

  int ret_char; // it is an int and not a char for safety reasons
  int option_index = 0;

  MPI_Init(&argc, &argv);
  MPI_Comm_size(MPI_COMM_WORLD, &nProcesses);
  MPI_Comm_rank(MPI_COMM_WORLD, &myRank);

  while ((ret_char = getopt_long(argc, argv, "P:V:W:L:n:N:d:D:u:U:t:",
                                 longOptions, &option_index)) != -1) {
    switch (ret_char) {
    case 0:
      if(debugFlag && myRank == 0){
	printf("Recognized option \"%s\"\n", longOptions[option_index].name);
      }
      break;
    case 'P':
      P = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Number of people: %d\n", P);
      }
      break;
    case 'V':
      V = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Number of vehicles: %d\n", V);
      }
      break;
    case 'W':
      W = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Width of the region: %d m\n", W);
      }
      break;
    case 'L':
      L = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Length of the region: %d m\n", L);
      }
      break;
    case 'n':
      Np = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Noise per person: %d dB\n", Np);
      }
      break;
    case 'N':
      Nv = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Noise per vehicle: %d dB\n", Nv);
      }
      break;
    case 'd':
      Dp = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Radius of a person: %d m\n", Dp);
      }
      break;
    case 'D':
      Dv = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Radius of a vehicle: %d m\n", Dv);
      }
      break;
    case 'u':
      Vp = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Speed of a person: %d m/s\n", Vp);
      }
      break;
    case 'U':
      Vv = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Speed of a vehicle: %d m/s\n", Vv);
      }
      break;
    case 't':
      t = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Time step: %d s\n", t);
      }
      break;
    case '?':
      if (myRank == 0) {
        printf("Option \"%s\" caused an error.\n",
               longOptions[option_index].name);
        printf("opterr: %d\noptopt: %d\n", opterr, optopt);
	printf("ret_char: %d (%c)\n", ret_char, ret_char);
      }
      break;
    default:
      if (myRank == 0) {
        printf("Option \"%s\" caused an error not recognized by getopt!\n",
               longOptions[option_index].name);
        printf("opterr: %d\noptopt: %d\n", opterr, optopt);
        printf("ret_char: %d (%c)\n", ret_char, ret_char);
      }
      break;
    }
  }


  printf("Hi, I'm process %d out of %d.\n", myRank, nProcesses);

  MPI_Finalize();

  return 0;
}
