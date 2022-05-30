#include <arpa/inet.h>
#include <errno.h>
#include <getopt.h>
#include <math.h>
#include <mpi.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#define N_INT_PARAMETERS 4
#define N_FLOAT_PARAMETERS 9
#define DEGREES_TO_METERS_MULTIPLIER 111120
#define METERS_TO_DEGREES_MULTIPLIER 0.000008999
#define RECORD_BUFFER_SIZE 100
#define PI 3.14159265
#define KAFKA_BRIDGE_DEFAULT_PORT 9999
#define KAFKA_BRIDGE_DEFAULT_ADDR "127.0.0.1"
// distance (in m) between contiguous virtual "sensors"
#define VIRTUAL_SENSOR_SPACING 10

// general TODO: why sometimes people don't move?

struct agent {
  float x;
  float y;
};

int missing_parameter();
void initialize_parameters();
void do_simulation(int vehicles_quota, int people_quota, int myRank);
void produce_sensor_data(struct agent *people, struct agent *vehicles,
                         int people_quota, int vehicles_quota, int socket_fd,
                         struct sockaddr_in server_addr);
void initialize_random_coordinates(struct agent *agents, int agents_quota);
void advance_person(struct agent *agent);
void advance_vehicle(struct agent *agent);
float distance(struct agent *agent, int x_sensor, int y_sensor);
float intensity(float decibels);
float intensity_at_distance(float intensity, float distance);
float decibels(float intensity);
void print_region(struct agent *people, struct agent *vehicles);
void populate_new_record(char *record, float x_coordinate, float y_coordinate,
                         float noise);
float lat_coordinate(float origin_coordinate, float offset_meters);
float lon_coordinate(float origin_coordinate, float offset_meters,
                     float latitude);
float secf(float angle);
float degrees(float radians);
float radians(float degrees);

enum int_parameter_idx { P, V, W, L };
enum float_parameter_idx { Np, Nv, Dp, Dv, Vp, Vv, t, lat, lon };
enum direction { UP, DOWN, LEFT, RIGHT };
static int debugFlag = 0;

/*simulation parameters
  Having them as global variables is ugly,
  but it allows to differenciate between integer and real parameters */
int int_parameters[N_INT_PARAMETERS];
float float_parameters[N_FLOAT_PARAMETERS];

float intensity_ref = 0.000000000001;
float intensity_person;
float intensity_vehicle;
float minimum_noise = 0.0;

// network address data
int kafka_bridge_port;
char kafka_bridge_address_string[16];

int main(int argc, char *argv[]) {
  int myRank;
  int nProcesses;

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

      {"origin-latitude", required_argument, NULL, 'o'},
      {"origin-longitude", required_argument, NULL, 'O'},

      {"kafka-bridge-address", required_argument, NULL, 'a'},
      {"kafka-bridge-port", required_argument, NULL, 'p'},

      {0, 0, 0, 0}};

  int ret_char; // it is an int and not a char for safety reasons
  int option_index = 0;

  // initialize parameters vector
  initialize_parameters();

  MPI_Init(&argc, &argv);
  MPI_Comm_size(MPI_COMM_WORLD, &nProcesses);
  MPI_Comm_rank(MPI_COMM_WORLD, &myRank);

  // initialize kafka network address to default values
  kafka_bridge_port = KAFKA_BRIDGE_DEFAULT_PORT;
  strcpy(kafka_bridge_address_string, KAFKA_BRIDGE_DEFAULT_ADDR);

  while ((ret_char = getopt_long(argc, argv, "P:V:W:L:n:N:d:D:u:U:t:a:p:",
                                 longOptions, &option_index)) != -1) {
    switch (ret_char) {
    case 0:
      if (debugFlag && myRank == 0) {
        printf("Recognized option \"%s\"\n", longOptions[option_index].name);
      }
      break;
    case 'P':
      int_parameters[P] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Number of people: %d\n", int_parameters[P]);
      }
      break;
    case 'V':
      int_parameters[V] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Number of vehicles: %d\n", int_parameters[V]);
      }
      break;
    case 'W':
      int_parameters[W] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Width of the region: %d m\n", int_parameters[W]);
      }
      break;
    case 'L':
      int_parameters[L] = atoi(optarg);
      if (debugFlag && myRank == 0) {
        printf("Length of the region: %d m\n", int_parameters[L]);
      }
      break;
    case 'n':
      float_parameters[Np] = atof(optarg);
      if (debugFlag && myRank == 0) {
        printf("Noise per person: %f dB\n", float_parameters[Np]);
      }
      break;
    case 'N':
      float_parameters[Nv] = atof(optarg);
      if (debugFlag && myRank == 0) {
        printf("Noise per vehicle: %f dB\n", float_parameters[Nv]);
      }
      break;
    case 'd':
      float_parameters[Dp] = atof(optarg);
      if (debugFlag && myRank == 0) {
        printf("Radius of a person: %f m\n", float_parameters[Dp]);
      }
      break;
    case 'D':
      float_parameters[Dv] = atof(optarg);
      if (debugFlag && myRank == 0) {
        printf("Radius of a vehicle: %f m\n", float_parameters[Dv]);
      }
      break;
    case 'u':
      float_parameters[Vp] = atof(optarg);
      if (debugFlag && myRank == 0) {
        printf("Speed of a person: %f m/s\n", float_parameters[Vp]);
      }
      break;
    case 'U':
      float_parameters[Vv] = atof(optarg);
      if (debugFlag && myRank == 0) {
        printf("Speed of a vehicle: %f m/s\n", float_parameters[Vv]);
      }
      break;
    case 't':
      float_parameters[t] = atof(optarg);
      if (debugFlag && myRank == 0) {
        printf("Time step: %f s\n", float_parameters[t]);
      }
      break;
    case 'o':
      float_parameters[lat] = atof(optarg);
      if (myRank == 0 && debugFlag) {
        printf("Latitude of area origin: %f N\n", float_parameters[lat]);
      }
      break;
    case 'O':
      float_parameters[lon] = atof(optarg);
      if (myRank == 0 && debugFlag) {
        printf("Longitude of area origin: %f E\n", float_parameters[lon]);
      }
      break;
    case 'a':
      strcpy(kafka_bridge_address_string, optarg);
      if (myRank == 0 && debugFlag) {
        printf("Changing network address of Kafka bridge to %s\n",
               kafka_bridge_address_string);
      }
      break;
    case 'p':
      kafka_bridge_port = atoi(optarg);
      if (myRank == 0 && debugFlag) {
        printf("Changing network port of Kafka bridge to %d\n",
               kafka_bridge_port);
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
  if ((missing_param = missing_parameter())) {
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

  // number of vehicles each process will track
  // TODO: check divisibility!
  int vehicles_quota = int_parameters[V] / nProcesses;

  // number of people each process will track
  // TODO: check divisibility!
  int people_quota = int_parameters[P] / nProcesses;

  intensity_person = intensity(float_parameters[Np]);
  intensity_vehicle = intensity(float_parameters[Nv]);

  // make sure each process have a copy of the parameters
  MPI_Bcast(int_parameters, N_INT_PARAMETERS, MPI_INT, 0, MPI_COMM_WORLD);
  MPI_Bcast(float_parameters, N_FLOAT_PARAMETERS, MPI_FLOAT, 0, MPI_COMM_WORLD);
  MPI_Bcast(&vehicles_quota, 1, MPI_INT, 0, MPI_COMM_WORLD);
  MPI_Bcast(&people_quota, 1, MPI_INT, 0, MPI_COMM_WORLD);
  MPI_Bcast(&intensity_person, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);
  MPI_Bcast(&intensity_vehicle, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);
  MPI_Bcast(kafka_bridge_address_string, strlen(kafka_bridge_address_string),
            MPI_CHAR, 0, MPI_COMM_WORLD);
  MPI_Bcast(&kafka_bridge_port, 1, MPI_INT, 0, MPI_COMM_WORLD);

  // Can be used as tests for the noise level calculations
  /*
  printf("Sum of two noises of intensity 10 dB is %f dB\n",
         decibels(intensity(10) * 2));
  printf("Noise of intensity 10 dB at distance doubled is %f dB\n",
         decibels(intensity_at_distance(intensity(10), 2)));
  */
  do_simulation(vehicles_quota, people_quota, myRank);
  MPI_Finalize();
  return 0;
}

/* To initialize the parameters vectors to 0.
   It must be called before parsing the command line options */

void initialize_parameters() {
  for (int i = 0; i < N_INT_PARAMETERS; i++) {
    int_parameters[i] = 0;
  }
  for (int i = 0; i < N_FLOAT_PARAMETERS; i++) {
    float_parameters[i] = 0.0;
  }
  return;
}

/* Checks the parameters have all been specified. If a missing parameter is
   found, it is returned the index (as specified in the parameters enum. If
   everything is fine, it returns 0 */

int missing_parameter() {
  for (int i = 0; i < N_INT_PARAMETERS; i++) {
    if (int_parameters[i] == 0) {
      return i + 1; // returning i doesn't signal error if i == 0
    }
  }
  for (int i = 0; i < N_FLOAT_PARAMETERS; i++) {
    if (float_parameters[i] == 0) {
      if( (i == lat || i == lon) && debugFlag){
	printf("WARNING: coordinate set to 0\n");
      }else {
	// returning i doesn't signal error if i == 0
	return i + N_INT_PARAMETERS + 1;
      }
    }
  }
  return 0;
}

/* Initializes the vector of agents (people or vehicles) with random initial
 * coordinates in the range [0, max_length - 1] (for the x), [0, max_width - 1]
 * (for the y). For simplicity, the initial coordinates are integers */

void initialize_random_coordinates(struct agent *agents, int agents_quota) {
  for (int i = 0; i < agents_quota; i++) {
    agents[i].x = rand() % int_parameters[L];
    agents[i].y = rand() % int_parameters[W];
  }
  return;
}

/* Performs the simulation, in an unbound loop */

void do_simulation(int vehicles_quota, int people_quota, int myRank) {
  struct agent people[people_quota];
  struct agent vehicles[vehicles_quota];

  initialize_random_coordinates(people, people_quota);
  initialize_random_coordinates(vehicles, vehicles_quota);

  struct sockaddr_in kafka_bridge_addr;
  kafka_bridge_addr.sin_family = AF_INET;
  kafka_bridge_addr.sin_port = htons(kafka_bridge_port);
  inet_pton(AF_INET, kafka_bridge_address_string, &kafka_bridge_addr.sin_addr);

  int socket_fd = socket(AF_INET, SOCK_STREAM, 0);

  int errConnection = connect(socket_fd, (struct sockaddr *)&kafka_bridge_addr,
                              sizeof(kafka_bridge_addr));
  if (errConnection != 0) { // there was an error
    printf("Unable to connect: errno %d\n", errno);
    return;
  } else {

    while (1) { // for each time step
      for (int i = 0; i < people_quota; i++) {
        advance_person(people + i);
      }
      for (int i = 0; i < vehicles_quota; i++) {
        advance_vehicle(vehicles + i);
      }
      if (debugFlag && myRank == 0)
        print_region(people, vehicles);
      produce_sensor_data(people, vehicles, people_quota, vehicles_quota,
                          socket_fd, kafka_bridge_addr);
      sleep(float_parameters[t]);
    }
  }
}

/* Advance a person of a step, calculated as Vp * t
   WARNING: To speed up calculations, the check on boundaries is not cyclic,
   i.e. it assumes that an agent cannot do more than a single "wrap" per turn,
   i.e. the velocity is minor, in modulus, of both the region dimensions */

void advance_person(struct agent *agent) {
  enum direction dir = rand() % 4;

  switch (dir) {
  case RIGHT:
    agent->x = agent->x + (float_parameters[Vp] * float_parameters[t]);
    if (agent->x > int_parameters[L])
      agent->x -= int_parameters[L];
    break;
  case LEFT:
    agent->x = agent->x - (float_parameters[Vp] * float_parameters[t]);
    if (agent->x < 0)
      agent->x += int_parameters[L];
    break;
  case UP:
    agent->y = agent->y + (float_parameters[Vp] * float_parameters[t]);
    if (agent->y > int_parameters[W])
      agent->y -= int_parameters[W];
  case DOWN:
    agent->y = agent->y - (float_parameters[Vp] * float_parameters[t]);
    if (agent->y < 0)
      agent->y += int_parameters[W];
    break;
  }
}

/* Advance a vehicle of a step, calculated as Vv * t
   WARNING: To speed up calculations, the check on boundaries is not cyclic,
   i.e. it assumes that an agent cannot do more than a single "wrap" per turn,
   i.e. the velocity is minor, in modulus, of both the region dimensions */

void advance_vehicle(struct agent *agent) {
  enum direction dir = rand() % 4;

  switch (dir) {
  case RIGHT:
    agent->x = agent->x + (float_parameters[Vv] * float_parameters[t]);
    if (agent->x > int_parameters[L])
      agent->x -= int_parameters[L];
    break;
  case LEFT:
    agent->x = agent->x - (float_parameters[Vv] * float_parameters[t]);
    if (agent->x < 0)
      agent->x += int_parameters[L];
    break;
  case UP:
    agent->y = agent->y + (float_parameters[Vv] * float_parameters[t]);
    if (agent->y > int_parameters[W])
      agent->y -= int_parameters[W];
    break;
  case DOWN:
    agent->y = agent->y - (float_parameters[Vv] * float_parameters[t]);
    if (agent->y < 0)
      agent->y += int_parameters[W];
    break;
  }
}

/* Temporary way to visualize the simulation */

void print_region(struct agent *people, struct agent *vehicles) {
  for (int y = 0; y < int_parameters[W]; ++y) {
    for (int x = 0; x < int_parameters[L]; ++x) {
      int found_agent = 0;
      for (int p = 0; p < int_parameters[P]; p++) {
        // TODO: fix float comparison
        if (people[p].x == x && people[p].y == y) {
          found_agent = 1;
          putchar('p');
        }
      }
      for (int v = 0; v < int_parameters[V]; v++) {
        // TODO: fix float comparison
        if (vehicles[v].x == x && vehicles[v].y == y) {
          found_agent = 1;
          putchar('V');
        }
      }
      if (!found_agent)
        putchar(' ');
    }
    puts("|");
  }
  for (int x = 0; x < int_parameters[L]; x++) {
    putchar('-');
  }
  putchar('\n');
}

/* Returns the distance of an agent from the sensor
   placed at coordinates (x_sensor, y_sensor) */
float distance(struct agent *agent, int x_sensor, int y_sensor) {
  float delta_x = agent->x - x_sensor;
  float delta_y = agent->y - y_sensor;
  return sqrt(delta_x * delta_x + delta_y * delta_y);
}

/* Converts the intensity level of a sound from dB to W */
float intensity(float decibels) {
  return powf(10, decibels / 10)
      // technically, this should be a part of calculation,
      // but if we simplify it everywhere there's no problem
      //       * intensity_ref
      ;
}

/* Calculates the attenuation given by the distance from source to sensor */
float intensity_at_distance(float intensity, float distance) {
  return intensity / ((distance + 1) * (distance + 1));
}

/* Converts the intenisty level of a sound from W to dB */
float decibels(float intensity) {
  return 10 * log10f(intensity
                     // technically, this should be a part of calculation,
                     // but if we simplify it everywhere there's no problem
                     //		     /intensity_ref
              );
}

/* Produce the noise data detected by each simulated sensor,
  placed at integer coordinates in the area */
void produce_sensor_data(struct agent *people, struct agent *vehicles,
                         int people_quota, int vehicles_quota, int socket_fd,
                         struct sockaddr_in server_addr) {
  for (int x_sensor = 0; x_sensor <= int_parameters[L]; x_sensor += VIRTUAL_SENSOR_SPACING) {
    for (int y_sensor = 0; y_sensor <= int_parameters[W]; y_sensor += VIRTUAL_SENSOR_SPACING) {
      float dist;
      float intensity_sensor = 0.0;

      for (int p_idx = 0; p_idx < people_quota; p_idx++) {
        dist = distance(people + p_idx, x_sensor, y_sensor);
        if (dist <= float_parameters[Dp]) {
          intensity_sensor += intensity_at_distance(intensity_person, dist);
          // diagnostic (may be removed or guarded)
          if (debugFlag && !isfinite(intensity_sensor))
            printf("ERROR: generating intensity_sensor is %f\n",
                   intensity_sensor);
        }
      }
      for (int v_idx = 0; v_idx < vehicles_quota; v_idx++) {
        dist = distance(vehicles + v_idx, x_sensor, y_sensor);
        if (dist <= float_parameters[Dv]) {
          intensity_sensor += intensity_at_distance(intensity_vehicle, dist);
          // diagnostic (may be removed or guarded)
          if (debugFlag && !isfinite(intensity_sensor))
            printf("ERROR: generating intensity_sensor is %f\n",
                   intensity_sensor);
        }
      }

      float noise_sensor = decibels(intensity_sensor);
      if (debugFlag &&
          (noise_sensor == INFINITY)) { // not the best, but we need to check
                                        // only INFINITY, while the negative case
                                        // is handled later
        printf("ERROR: noise_sensor is %f, and generating intensity_sensor is "
               "%f\n",
               noise_sensor, intensity_sensor);
      }
      if (noise_sensor < 0){
	noise_sensor = 0.0;
	if(debugFlag){
	  printf("INFO: setting negative noise to 0.0\n");
	}
      }
      // TODO: send noise_sensor to coordinator process?
      // TODO DOING: and then to the Kafka cluster
      char record[RECORD_BUFFER_SIZE];
      float y_coordinate = lat_coordinate(float_parameters[lat], y_sensor);
      float x_coordinate =
          lon_coordinate(float_parameters[lon], x_sensor, y_coordinate);

      populate_new_record(record, x_coordinate, y_coordinate, noise_sensor);
      send(socket_fd, record, strlen(record), 0);

      if(debugFlag && noise_sensor != -INFINITY && noise_sensor < minimum_noise){
	minimum_noise = noise_sensor;
	printf("new minimum: %f\n", minimum_noise);
      }
      if (debugFlag) {
	printf("TEST: %s\n", record);
      }
    }
  }
}

void populate_new_record(char *record, float x_coordinate, float y_coordinate,
                         float noise) {
  sprintf(record, "{\"x\":%f,\"y\":%f,\"val\":%f}\n", x_coordinate,
          y_coordinate, noise);
}

float lat_coordinate(float origin_coordinate, float offset_meters) {
  return origin_coordinate + offset_meters * METERS_TO_DEGREES_MULTIPLIER;
}

float lon_coordinate(float origin_coordinate, float offset_meters,
                     float latitude) {
  float meters_per_lon_degree =
      (DEGREES_TO_METERS_MULTIPLIER / secf(radians(latitude)));
  float lon_degrees_per_meter = 1 / meters_per_lon_degree;
  return origin_coordinate + offset_meters * lon_degrees_per_meter;
}

float secf(float angle) { return 1 / cosf(angle); }

float degree(float radians) { return radians * 180 / PI; }

float radians(float degrees) { return degrees * PI / 180; }
