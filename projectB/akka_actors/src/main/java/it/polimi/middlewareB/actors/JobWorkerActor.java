package it.polimi.middlewareB.actors;

import akka.actor.AbstractActorWithStash;
import it.polimi.middlewareB.JobExecutionException;
import it.polimi.middlewareB.messages.DocumentConversionJobMessage;
import it.polimi.middlewareB.messages.ImageCompressionJobMessage;
import it.polimi.middlewareB.messages.JobCompletedMessage;
import it.polimi.middlewareB.messages.TextFormattingJobMessage;

import java.util.Random;

public class JobWorkerActor extends AbstractActorWithStash {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TextFormattingJobMessage.class, this::echoTextFormattingMessage)
                .match(DocumentConversionJobMessage.class, this::echoDocumentConversionMessage)
                .match(ImageCompressionJobMessage.class, this::echoImageCompressionMessage)
                .build();
    }

    public void echoTextFormattingMessage(TextFormattingJobMessage msg) throws JobExecutionException {
        if(random.nextDouble() < probabilityOfFailure){
            stash();
            System.out.println("Failure!");
            throw new JobExecutionException("Exception in Text Formatting");
        }
        String completionMessage = "Received " + msg.getClass().getName() + ": " +
                "input " + msg.getInputFile() + ", " +
                "output " + msg.getOutputFile() + ", " +
                "formatting rules " + msg.getFormattingRules();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(completionMessage);
        sender().tell(new JobCompletedMessage(completionMessage), self());

    }

    public void echoDocumentConversionMessage(DocumentConversionJobMessage msg) throws JobExecutionException {
        if(random.nextDouble() < probabilityOfFailure){
            stash();
            System.out.println("Failure!");
            throw new JobExecutionException("Exception in Document Conversion");
        }
        String completionMessage = "Received " + msg.getClass().getName() + ": " +
                "input " + msg.getInputFile() + ", " +
                "output " + msg.getOutputFile() + ", " +
                "target extension " + msg.getTargetExtension();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(completionMessage);
        sender().tell(new JobCompletedMessage(completionMessage), self());
    }

    public void echoImageCompressionMessage(ImageCompressionJobMessage msg) throws JobExecutionException {
        if(random.nextDouble() < probabilityOfFailure){
            stash();
            System.out.println("Failure!");
            throw new JobExecutionException("Exception in Image Compression");
        }
        String completionMessage = "Received " + msg.getClass().getName() + ": " +
                "input " + msg.getInputFile() + ", " +
                "output " + msg.getOutputFile() + ", " +
                "compression ratio " + msg.getCompressionRatio();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(completionMessage);
        sender().tell(new JobCompletedMessage(completionMessage), self());

    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        //TODO add a seed as parameter
        random = new Random();
    }


    @Override
    public void postRestart(Throwable reason) throws Exception {
        super.postRestart(reason);
        unstashAll();
    }

    private static final double probabilityOfFailure = 0.2;
    private static Random random;
}
