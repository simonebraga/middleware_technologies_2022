package it.polimi.middlewareB.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import it.polimi.middlewareB.messages.TextMessage;

public class JobWorkerActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(TextMessage.class, this::echo)
				.build();
	}

	public void echo(TextMessage msg) {
		System.out.println(msg.getText());

	}

	public static Props props() {
		return Props.create(JobWorkerActor.class);
	}

}
