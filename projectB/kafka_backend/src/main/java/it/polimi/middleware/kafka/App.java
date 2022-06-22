package it.polimi.middleware.kafka;

import it.polimi.middleware.kafka.utils.Job;
import it.polimi.middleware.kafka.utils.JobList;
import it.polimi.middleware.kafka.utils.ListeningDaemon;
import it.polimi.middleware.kafka.utils.NotificationList;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;

public class App {

    private static final NotificationList notificationList = new NotificationList();

    // This method generates a random string that is used as key for the jobs
    private static String keyGen(int keyLength) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < keyLength; i++) {
            int rand = (int) (Math.random() * 62);
            if (rand <= 9) rand+=48;
            else if (rand <= 35) rand+=55;
            else rand+=61;
            output.append((char) rand);
        } return output.toString();
    }

    private static void printInstructions(JobList jobList) {
        System.out.println("\nType \"quit\" to stop the application or run one of the following commands:\n" +
                "\t\"submit <JOB_TYPE>\" - Submit a job to the back-end. Remember the job ID to get your result!\n" +
                "\t\"retrieve <JOB_ID>\" - Add the given ID to the list of jobs you want to be notified when completed.\n" +
                "\t\"list\" - Print the notification list.\n" +
                "\t\"help\" - List of the possible commands.\n");
        System.out.println("<JOB_TYPE> shall be replaced with one of the following:");
        for (Job job : jobList.getJobList()) {
            System.out.println("\t\"" + job.getId() + "\" <SOURCE_IMAGE> <" + job.getParam_name() + "> <RESULT_FOLDER>");
        } System.out.println();
    }

    private static void submittedJob(String key) {
        System.out.println("\nNew job submitted.\n" +
                "The ID of your job <" + key + "> was automatically added to the notification list.\n" +
                "Type \"help\" for the list of commands.\n");
    }

    private static void retrievedJob(String key) {
        System.out.println("\nThe ID <" + key + "> was added to the notification list.\n" +
                "Type \"help\" for the list of commands.\n");
    }

    private static void badSyntax() {
        System.out.println("\nBad syntax.\n" +
                "Type \"help\" for the list of commands.\n");
    }

    private static void unknownCommand() {
        System.out.println("\nUnknown command.\n" +
                "Type \"help\" for the list of commands.\n");
    }

    private static void notExistingJob() {
        System.out.println("\nYou tried to submit a job that doesn't exist! Operation aborted.\n" +
                "Type \"help\" for the list of commands.\n");
    }

    private static void printList() {
        String[] list = notificationList.get();
        if (list.length > 0) {
            System.out.println("\nYou will be notified about the completion of the following jobs:");
            for (String s : list)
                System.out.print(s + " ");
            System.out.println();
        } else System.out.println("\nThe notification list is empty. You won't get any notification!");
        System.out.println("Type \"help\" for the list of commands.\n");
    }

    public static void main( String[] args ) {

        // Use default values if not specified otherwise
        // Default values work in a local environment in IntelliJ
        final String bootstrap = args.length > 0 ? args[0] : "localhost:9092";
        final String topic_out = args.length > 1 ? args[1] : "pendingJobs";
        final String topic_in = args.length > 2 ? args[2] : "completedJobs";
        final String mapFilePath = args.length > 3 ? args[3] : "src/main/resources/";
        final String mapFileName = args.length > 4 ? args[4] : "job_list.json";
        final int keyLength = args.length > 5 ? Integer.parseInt(args[5]) : 8;
        final boolean debugMode = args.length <= 6 || Boolean.parseBoolean(args[6]);

        JobList jobList = new JobList();
        jobList.initJobList(mapFilePath + mapFileName);

        // If the flag for the debug mode is set, run a thread that simulates the completion of jobs
        if (debugMode) {
            Runnable demo = new Demo(topic_out, topic_in, bootstrap);
            new Thread(demo).start();
        }

        // Set the properties for the producer (needed to submit pending jobs)
        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        final KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        // Set the properties for the consumer (needed to retrieve completed jobs)
        final Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "cli");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);

        consumer.subscribe(Collections.singletonList(topic_in));

        // Run a daemon that periodically checks the job completion
        Runnable daemon = new ListeningDaemon(consumer, notificationList);
        new Thread(daemon).start();

        // Effective implementation of the CLI
        System.out.println("[LOG] Application started");
        printInstructions(jobList);
        Scanner in = new Scanner(System.in); String s; String[] split; String key;
        while (!(s=in.nextLine().trim()).equals("quit")) {
            split = s.split("\\s+");
            switch (split[0]) {
                case "submit":
                    if (split.length == 5) {
                        if (jobList.getJobArrayList().contains(split[1])) {
                            key = keyGen(keyLength);
                            producer.send(new ProducerRecord<>(topic_out, key,
                                    "{\"job_type\":\"" + split[1] +
                                            "\",\"source\":\"" + split[2] +
                                            "\",\"parameters\":\"" + split[3] +
                                            "\",\"results\":\"" + split[4] + "\"}"));
                            notificationList.add(key);
                            submittedJob(key);
                        } else notExistingJob();
                    } else badSyntax();
                    break;
                case "retrieve":
                    if (split.length == 2) {
                        notificationList.add(split[1]);
                        retrievedJob(split[1]);
                    } else badSyntax();
                    break;
                case "list":
                    if (split.length == 1)
                        printList();
                    else badSyntax();
                    break;
                case "help":
                    if (split.length == 1)
                        printInstructions(jobList);
                    else badSyntax();
                    break;
                default:
                    unknownCommand();
            }
        }

        System.exit(0);
    }
}
