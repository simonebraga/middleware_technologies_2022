package it.polimi.middlewareB.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import it.polimi.middlewareB.messages.DocumentConversionJobMessage;
import it.polimi.middlewareB.messages.ImageCompressionJobMessage;
import it.polimi.middlewareB.messages.TextFormattingJobMessage;

public class JobWorkerActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(TextFormattingJobMessage.class, this::echoTextFormattingMessage)
				.match(DocumentConversionJobMessage.class, this::echoDocumentConversionMessage)
				.match(ImageCompressionJobMessage.class, this::echoImageCompressionMessage)
				.build();
	}

	public void echoTextFormattingMessage(TextFormattingJobMessage msg) {
		System.out.println("Received " + msg.getClass().getName() + ": " +
				"input " + msg.getInputFile() + ", " +
				"output " + msg.getOutputFile() + ", " +
				"formatting rules " + msg.getFormattingRules());

	}

	public void echoDocumentConversionMessage(DocumentConversionJobMessage msg) {
		System.out.println("Received " + msg.getClass().getName() + ": " +
				"input " + msg.getInputFile() + ", " +
				"output " + msg.getOutputFile() + ", " +
				"target extension " + msg.getTargetExtension());
	}

	public void echoImageCompressionMessage(ImageCompressionJobMessage msg) {
		System.out.println("Received " + msg.getClass().getName() + ": " +
				"input " + msg.getInputFile() + ", " +
				"output " + msg.getOutputFile() + ", " +
				"compression ratio " + msg.getCompressionRatio());

	}

	public static Props props() {
		return Props.create(JobWorkerActor.class);
	}

}
