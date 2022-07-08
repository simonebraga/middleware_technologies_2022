package it.polimi.middlewareB;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import akka.routing.BalancingPool;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxPool;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import it.polimi.middlewareB.actors.JobSupervisorActor;
import it.polimi.middlewareB.analysis.CompletedAnalysisThread;
import it.polimi.middlewareB.analysis.PendingAnalysisThread;
import it.polimi.middlewareB.analysis.RetriesAnalysisActor;
import it.polimi.middlewareB.messages.JobTaskMessage;
import it.polimi.middlewareB.messages.KafkaConfigurationMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ClusterStarter {

	public static void main(String[] args) {
		final String bootstrap = args.length > 0 ? args[0] : "localhost:9092";

		Config config = ConfigFactory.parseFile(new File("Remote_configuration.txt"));
		String jobJSONFile = "src/main/resources/job_list.json";
		Map<String, Integer> jobDurations;
		try {
			jobDurations = populateMap(jobJSONFile);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}

		//ActorSystem sys = ActorSystem.create("ProjB_actor_system", config);
		ActorSystem sys = ActorSystem.create("ProjB_actor_system");

		ActorRef retriesAnalysisActor = sys.actorOf(RetriesAnalysisActor.props());
		//It would be best using a BalancingPool
		ArrayList<ActorRef> actors = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			actors.add(sys.actorOf(JobSupervisorActor.props(bootstrap,retriesAnalysisActor,jobDurations), "supervisor" + i));
		}

		Runnable completedAnalysis = new CompletedAnalysisThread(bootstrap, retriesAnalysisActor);
		new Thread(completedAnalysis).start();
		Runnable pendingAnalysis = new PendingAnalysisThread(bootstrap);
		new Thread(pendingAnalysis).start();

		//TODO Change with sleep
		while (true);
		//testBasicMessages(mainDispatcher, jobDurations);
		//processPendingJobs(mainDispatcher, bootstrap, jobDurations);
		//sys.terminate();
		// Scanner keyboard = new Scanner(System.in);
		// keyboard.nextLine();
		// keyboard.close();
		//return;
	}

	private static Map<String, Integer> populateMap(String pathname) throws IOException {
		ObjectMapper jacksonMapper = new ObjectMapper();
		JSONJobDuration[] jobs;

		Map<String, Integer> jobDurations = new HashMap<>();

		try {
			jobs = jacksonMapper
					.readValue(new File(pathname), JSONJobDuration[].class);
			for (JSONJobDuration jt : jobs) {
				jobDurations.put(jt.getName(), jt.getDuration());
			}
		} catch (JsonMappingException e) {
			System.err.println("[ERROR] Fatal problem with mapping of map file content!");
		} catch (JsonParseException e) {
			System.err.println("[ERROR] Map file is bad formed!");
		} catch (IOException e) {
			System.err.println("IOException reading json file!");
			throw new RuntimeException(e);
		}
		if (jobDurations.isEmpty()){
			throw new IOException("Something went wrong in parsing the JSON file!");
		}
		return jobDurations;
	}
	public static final Duration MAX_DURATION = Duration.ofNanos(Long.MAX_VALUE);

	private static void testBasicMessages(ActorRef mainDispatcher, Map<String, Integer> jobDurations) {
		System.out.println("Sending a basic message...");

		// TEST CODE //
		String repeatedJob = "compilation";
		//mainDispatcher.tell(new JobTaskMessage("dummyKey1", repeatedJob, "src", "target", "gcc", jobDurations.get(repeatedJob)), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey2", repeatedJob, "src", "target", "gcc", jobDurations.get(repeatedJob)), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey3", repeatedJob, "src", "target", "gcc", jobDurations.get(repeatedJob)), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey4", repeatedJob, "src", "target", "gcc", jobDurations.get(repeatedJob)), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey5", repeatedJob, "src", "target", "gcc", jobDurations.get(repeatedJob)), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey6", repeatedJob, "src", "target", "gcc", jobDurations.get(repeatedJob)), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey7", repeatedJob, "src", "target", "gcc", jobDurations.get(repeatedJob)), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey8", repeatedJob, "src", "target", "gcc", jobDurations.get(repeatedJob)), null);

//
		//mainDispatcher.tell(new JobTaskMessage("dummyKey1", "image-compression", "/another/input", "/another/output", "20", jobDurations.get("image-compression")), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey2", "text-formatting", "/another/input", "/another/output", "s/ /  /", jobDurations.get("text-formatting")), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey3", "document-conversion", "/another/input", "/another/output", ".pdf", jobDurations.get("document-conversion")), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey4", "image-compression", "/another/input", "/another/output", "40", jobDurations.get("image-compression")), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey5", "text-formatting", "/another/input", "/another/output", "grep akka", jobDurations.get("text-formatting")), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey6", "document-conversion", "/another/input", "/another/output", ".pdf", jobDurations.get("document-conversion")), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey7", "image-compression", "/another/input", "/another/output", "45", jobDurations.get("image-compression")), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey8", "text-formatting", "/another/input", "/another/output", "tee", jobDurations.get("text-formatting")), null);
		//mainDispatcher.tell(new JobTaskMessage("dummyKey9", "document-conversion", "/another/input", "/another/output", ".pdf", jobDurations.get("document-conversion")), null);

		System.out.println("Messages sent!");

		try {
			Thread.sleep(60_000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static void processPendingJobs(ActorRef mainDispatcher, String bootstrap, Map<String, Integer> jobDurations){
		/*try {
			System.setOut(new PrintStream(new FileOutputStream("/dev/null")));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}*/
		Properties config = new Properties();
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "tasksExecutors");
		config.put("bootstrap.servers", bootstrap);
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		config.setProperty("auto.offset.reset","earliest");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config);

		consumer.subscribe(List.of("pendingJobs"));
		//consumer.seek(new TopicPartition("pending"), 0);

		ObjectMapper jacksonMapper = new ObjectMapper();

		while(true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
			if (records.count() != 0) {
				for (ConsumerRecord<String, String> record : records) {
					LocalDateTime timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(record.timestamp()),
							TimeZone.getDefault().toZoneId());
					System.out.println(timestamp + " - key: " + record.key() + ", " + record.value());
					try {
						JSONJobTask nextJob = jacksonMapper.readValue(record.value(), JSONJobTask.class);
						buildAndSendMessage(mainDispatcher, record.key(), nextJob, jobDurations);
					} catch (JsonProcessingException e) {
						System.err.println("Unable to process job: " + record.key() + ", " + record.value());
						//throw new RuntimeException(e);
					}
				}
			}
			//System.out.println("Processing done!");
		}
	}

	private static void buildAndSendMessage(ActorRef mainDispatcher, String key, JSONJobTask jsonJob, Map<String, Integer> jobDurations){
		if (jobDurations.containsKey(jsonJob.getName())) {
			mainDispatcher.tell(new JobTaskMessage(key,
					jsonJob.getName(),
					jsonJob.getInput(),
					jsonJob.getOutput(),
					jsonJob.getParameter(),
					jobDurations.get(jsonJob.getName())), ActorRef.noSender());
		} else {
			System.err.println("Unknown job type! - " + jsonJob.getName());
		}
	}
}



