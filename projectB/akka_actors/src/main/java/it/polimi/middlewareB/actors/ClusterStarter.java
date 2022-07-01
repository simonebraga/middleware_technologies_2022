package it.polimi.middlewareB.actors;

import java.io.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import akka.routing.SmallestMailboxPool;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import it.polimi.middlewareB.JSONJobDuration;
import it.polimi.middlewareB.JSONJobTask;
import it.polimi.middlewareB.messages.DocumentConversionJobMessage;
import it.polimi.middlewareB.messages.ImageCompressionJobMessage;
import it.polimi.middlewareB.messages.TextFormattingJobMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ClusterStarter {

	public static void main(String[] args) {
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

		ActorRef mainDispatcher = sys.actorOf(new SmallestMailboxPool(3).props(JobSupervisorActor.props()),
				"roundrobinpool");

		//testBasicMessages(mainDispatcher, jobDurations);
		processPendingJobs(mainDispatcher, jobDurations);
		sys.terminate();
		// Scanner keyboard = new Scanner(System.in);
		// keyboard.nextLine();
		// keyboard.close();

		return;
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
		mainDispatcher.tell(new TextFormattingJobMessage("/simple/input", "simple/output", "s/\\t/    /", jobDurations.get("text-formatting")), null);

		mainDispatcher.tell(new ImageCompressionJobMessage("/input", "/output", 15, jobDurations.get("image-compression")), null);

		mainDispatcher.tell(
				new DocumentConversionJobMessage(
						"/another/input", "/another/output", ".pdf", jobDurations.get("document-conversion")),
				null);

		mainDispatcher.tell(new TextFormattingJobMessage("/simple/input", "simple/output", "s/\\t/    /", jobDurations.get("text-formatting")), null);

		mainDispatcher.tell(new ImageCompressionJobMessage("/input", "/output", 15, jobDurations.get("image-compression")), null);

		mainDispatcher.tell(new DocumentConversionJobMessage("/another/input", "/another/output", ".pdf", jobDurations.get("document-conversion")), null);
		mainDispatcher.tell(new TextFormattingJobMessage("/simple/input", "simple/output", "s/\\t/    /", jobDurations.get("text-formatting")), null);

		mainDispatcher.tell(new ImageCompressionJobMessage("/input", "/output", 15, jobDurations.get("image-compression")), null);

		mainDispatcher.tell(new DocumentConversionJobMessage("/another/input", "/another/output", ".pdf", jobDurations.get("document-conversion")), null);
		try {
			Thread.sleep(20_000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static void processPendingJobs(ActorRef mainDispatcher, Map<String, Integer> jobDurations){
		/*try {
			System.setOut(new PrintStream(new FileOutputStream("/dev/null")));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}*/
		Properties config = new Properties();
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "single1");
		config.put("bootstrap.servers", "localhost:9092");
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		config.setProperty("auto.offset.reset","earliest");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config);

		consumer.subscribe(List.of("pendingJobs"));
		//consumer.seek(new TopicPartition("pending"), 0);

		ObjectMapper jacksonMapper = new ObjectMapper();

		while(true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
			if (records.count() == 0) {

			} else {
				for (ConsumerRecord<String, String> record : records) {
					System.out.println("key: " + record.key() + ", " + record.value());
					try {
						JSONJobTask nextJob = jacksonMapper.readValue(record.value(), JSONJobTask.class);
						buildAndSendMessage(mainDispatcher, nextJob, jobDurations);
					} catch (JsonProcessingException e) {
						System.err.println("Unable to process job: " + record.key() + ", " + record.value());
						//throw new RuntimeException(e);
					}
				}
			}
			System.out.println("Processing done!");
		}
	}

	private static void buildAndSendMessage(ActorRef mainDispatcher, JSONJobTask jsonJob, Map<String, Integer> jobDurations){
		switch (jsonJob.getName()){
			case "image-compression":
				mainDispatcher
						.tell(new ImageCompressionJobMessage(jsonJob.getInput(),
								jsonJob.getOutput(),
								Integer.parseInt(jsonJob.getParameter()),
								jobDurations.get(jsonJob.getName())), ActorRef.noSender());
				break;
			case "text-formatting":
				mainDispatcher
						.tell(new TextFormattingJobMessage(jsonJob.getInput(),
								jsonJob.getOutput(),
								jsonJob.getParameter(),
								jobDurations.get(jsonJob.getName())), ActorRef.noSender());
				break;
			case "document-conversion":
				mainDispatcher
						.tell(new DocumentConversionJobMessage(jsonJob.getInput(),
								jsonJob.getOutput(),
								jsonJob.getParameter(),
								jobDurations.get(jsonJob.getName())), ActorRef.noSender());
				break;
			default:
				System.err.println("Unknown job type! - " + jsonJob.getName());

		}
	}
}


