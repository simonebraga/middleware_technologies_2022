package it.polimi.middlewareB.actors;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import akka.routing.BalancingPool;
import akka.routing.Pool;
import akka.routing.RoundRobinPool;
import akka.routing.SmallestMailboxPool;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import it.polimi.middlewareB.messages.DocumentConversionJobMessage;
import it.polimi.middlewareB.messages.ImageCompressionJobMessage;
import it.polimi.middlewareB.messages.TextFormattingJobMessage;

public class ClusterStarter {

	public static void main(String[] args) {
		Config config = ConfigFactory.parseFile(new File("Remote_configuration.txt"));
		String jobJSONFile = "src/main/resources/job_list.json";
		Map<String, Integer> jobMap;
		try {
			jobMap = populateMap(jobJSONFile);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}

		//ActorSystem sys = ActorSystem.create("ProjB_actor_system", config);
		ActorSystem sys = ActorSystem.create("ProjB_actor_system");

		ActorRef mainDispatcher = sys.actorOf(new SmallestMailboxPool(3).props(JobSupervisorActor.props()),
				"roundrobinpool");

		System.out.println("Sending a basic message...");

		// TEST CODE //
		mainDispatcher.tell(new TextFormattingJobMessage("/simple/input", "simple/output", "s/\\t/    /", jobMap.get("text-formatting")), null);

		mainDispatcher.tell(new ImageCompressionJobMessage("/input", "/output", 15, jobMap.get("image-compression")), null);

		mainDispatcher.tell(
				new DocumentConversionJobMessage(
						"/another/input", "/another/output", ".pdf", jobMap.get("document-conversion")),
				null);

		mainDispatcher.tell(new TextFormattingJobMessage("/simple/input", "simple/output", "s/\\t/    /", jobMap.get("text-formatting")), null);

		mainDispatcher.tell(new ImageCompressionJobMessage("/input", "/output", 15, jobMap.get("image-compression")), null);

		mainDispatcher.tell(new DocumentConversionJobMessage("/another/input", "/another/output", ".pdf", jobMap.get("document-conversion")), null);
		mainDispatcher.tell(new TextFormattingJobMessage("/simple/input", "simple/output", "s/\\t/    /", jobMap.get("text-formatting")), null);

		mainDispatcher.tell(new ImageCompressionJobMessage("/input", "/output", 15, jobMap.get("image-compression")), null);

		mainDispatcher.tell(new DocumentConversionJobMessage("/another/input", "/another/output", ".pdf", jobMap.get("document-conversion")), null);
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		sys.terminate();
		// Scanner keyboard = new Scanner(System.in);
		// keyboard.nextLine();
		// keyboard.close();

		return;
	}

	private static Map<String, Integer> populateMap(String pathname) throws IOException {
		ObjectMapper jacksonMapper = new ObjectMapper();
		JobType[] jobs;

		Map<String, Integer> jobMap = new HashMap<>();

		try {
			jobs = jacksonMapper
					.readValue(new File(pathname), JobType[].class);
			for (JobType jt : jobs) {
				jobMap.put(jt.getName(), jt.getDuration());
			}
		} catch (JsonMappingException e) {
			System.err.println("[ERROR] Fatal problem with mapping of map file content!");
		} catch (JsonParseException e) {
			System.err.println("[ERROR] Map file is bad formed!");
		} catch (IOException e) {
			System.err.println("IOException reading json file!");
			throw new RuntimeException(e);
		}
		if (jobMap.isEmpty()){
			throw new IOException("Something went wrong in parsing the JSON file!");
		}
		return jobMap;
	}
	public static final Duration MAX_DURATION = Duration.ofNanos(Long.MAX_VALUE);
}

