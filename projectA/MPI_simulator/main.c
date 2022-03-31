#include <getopt.h>
#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

#define N_PARAMETERS 11

int missing_parameter(int parameters[]);
void initialize_parameters(int parameters[]);

enum parameter { P, V, W, L, Np, Nv, Dp, Dv, Vp, Vv, t };

static int debugFlag = 0;

int main(int argc, char *argv[]) {
  int myRank;
  int nProcesses;

  // simulation parameters
  // NOTE: THEY ARE SIMPLE INTS FOR NOW!
  //  int P, V, W, L, Np, Nv, Dp, Dv, Vp, Vv, t;
  int parameters[N_PARAMETERS];

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

  // initialize parameters vector
  initialize_parameters(parameters);

  MPI_Init(&argc, &argv);
  MPI_Comm_size(MPI_COMM_WORLD, &nProcesses);
  MPI_Comm_rank(MPI_COMM_WORLD, &myRank);

  while ((ret_char = getopt_long(argc, argv, "P:V:W:L:n:N:d:D:u:U:t:",
                                 longOptions, &option_index)) != -1) {
    switch (ret_char) {
    case 0:
      if (debugFlag && myRank == 0) {
        printf("Recognized option \"%s\"\n", longOptions[option_index].name);
      }
      break;
    case 'P':
      parameters[P] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Number of people: %d\n", parameters[P]);
      }
      break;
    case 'V':
      parameters[V] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Number of vehicles: %d\n", parameters[V]);
      }
      break;
    case 'W':
      parameters[W] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Width of the region: %d m\n", parameters[W]);
      }
      break;
    case 'L':
      parameters[L] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Length of the region: %d m\n", parameters[L]);
      }
      break;
    case 'n':
      parameters[Np] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Noise per person: %d dB\n", parameters[Np]);
      }
      break;
    case 'N':
      parameters[Nv] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Noise per vehicle: %d dB\n", parameters[Nv]);
      }
      break;
    case 'd':
      parameters[Dp] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Radius of a person: %d m\n", parameters[Dp]);
      }
      break;
    case 'D':
      parameters[Dv] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Radius of a vehicle: %d m\n", parameters[Dv]);
      }
      break;
    case 'u':
      parameters[Vp] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Speed of a person: %d m/s\n", parameters[Vp]);
      }
      break;
    case 'U':
      parameters[Vv] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Speed of a vehicle: %d m/s\n", parameters[Vv]);
      }
      break;
    case 't':
      parameters[t] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Time step: %d s\n", parameters[t]);
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

  // check if all parameters have been specified
  int missing_param;
  if ((missing_param = missing_parameter(parameters))) {
    if (myRank == 0) {
      printf("Error! Parameter number %d is missing\n",
             missing_param - 1); // see the function
      printf("Exiting...\n");
    }
    MPI_Finalize();
    return 1;
  }

  // if everithing is ok, we can continue

  printf("Hi, I'm process %d out of %d.\n", myRank, nProcesses);

  MPI_Finalize();

  return 0;
}

/* To initialize the parameters vector to 0.
   It must be called before parsing the command line options */

void initialize_parameters(int parameters[]) {
  for (int i = 0; i < N_PARAMETERS; i++) {
    parameters[i] = 0;
  }
  return;
}

/* Checks the parameters have all been specified. If a missing parameter is
   found, it is returned the index (as specified in the parameters enum. If
   everything is fine, it returns 0 */

int missing_parameter(int parameters[]) {
  for (int i = 0; i < N_PARAMETERS; i++) {
    if (parameters[i] == 0) {
      return i + 1; // returning i doesn't signal error if i == 0
    }
  }
  return 0;
}
