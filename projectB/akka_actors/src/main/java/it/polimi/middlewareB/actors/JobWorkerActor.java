package it.polimi.middlewareB.actors;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import it.polimi.middlewareB.JobExecutionException;
import it.polimi.middlewareB.messages.DocumentConversionJobMessage;
import it.polimi.middlewareB.messages.ImageCompressionJobMessage;
import it.polimi.middlewareB.messages.TextFormattingJobMessage;
import scala.Option;

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
        if(random.nextDouble() > probabilityOfFailure){
            stash();
            System.out.println("Failure!");
            throw new JobExecutionException("Exception in Text Formatting");
        }
        System.out.println("Received " + msg.getClass().getName() + ": " +
                "input " + msg.getInputFile() + ", " +
                "output " + msg.getOutputFile() + ", " +
                "formatting rules " + msg.getFormattingRules());

    }

    public void echoDocumentConversionMessage(DocumentConversionJobMessage msg) throws JobExecutionException {
        if(random.nextDouble() > probabilityOfFailure){
            stash();
            System.out.println("Failure!");
            throw new JobExecutionException("Exception in Document Conversion");
        }
        System.out.println("Received " + msg.getClass().getName() + ": " +
                "input " + msg.getInputFile() + ", " +
                "output " + msg.getOutputFile() + ", " +
                "target extension " + msg.getTargetExtension());
    }

    public void echoImageCompressionMessage(ImageCompressionJobMessage msg) throws JobExecutionException {
        if(random.nextDouble() > probabilityOfFailure){
            stash();
            System.out.println("Failure!");
            throw new JobExecutionException("Exception in Image Compression");
        }
        System.out.println("Received " + msg.getClass().getName() + ": " +
                "input " + msg.getInputFile() + ", " +
                "output " + msg.getOutputFile() + ", " +
                "compression ratio " + msg.getCompressionRatio());

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
        unstash();
    }

    private static final double probabilityOfFailure = 0.5;
    private static Random random;
}
