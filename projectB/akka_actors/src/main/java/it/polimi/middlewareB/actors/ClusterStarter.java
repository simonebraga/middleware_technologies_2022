package it.polimi.middlewareB.actors;

import java.io.File;
import java.time.Duration;

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

		//ActorSystem sys = ActorSystem.create("ProjB_actor_system", config);
		ActorSystem sys = ActorSystem.create("ProjB_actor_system");

		ActorRef mainDispatcher = sys.actorOf(Props.create(MainDispatcher.class), "mainDispatcher");

		System.out.println("Sending a basic message...");

		mainDispatcher.tell(new TextFormattingJobMessage("/simple/input", "simple/output", "s/\t/    /"), null);

		mainDispatcher.tell(new ImageCompressionJobMessage("/input", "/output", 15), null);

		mainDispatcher.tell(new DocumentConversionJobMessage("/another/input", "/another/output", ".pdf"), null);

		sys.terminate();
		// Scanner keyboard = new Scanner(System.in);
		// keyboard.nextLine();
		// keyboard.close();

	}
}

