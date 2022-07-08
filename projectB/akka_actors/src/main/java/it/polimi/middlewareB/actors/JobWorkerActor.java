package it.polimi.middlewareB.actors;

import akka.actor.AbstractActorWithStash;
import it.polimi.middlewareB.JobExecutionException;
import it.polimi.middlewareB.messages.JobCompletedMessage;
import it.polimi.middlewareB.messages.JobTaskMessage;

import java.util.Random;

public class JobWorkerActor extends AbstractActorWithStash {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JobTaskMessage.class, this::echoJobTaskMessage)
                .build();
    }


    public void echoJobTaskMessage(JobTaskMessage msg) throws JobExecutionException {
        //System.out.println("Started execution");
        if(random.nextDouble() < probabilityOfFailure){
            msg.incFailures();
            stash();
            System.out.println("Failure!");
            throw new JobExecutionException("Exception in " + msg.getName());
        }
        String completionMessage = "Completed " + msg.getName() +
                ", key " + msg.getKey() + ": " +
                "input " + msg.getInputFile() + ", " +
                "output " + msg.getOutputFile() + ", " +
                "target extension " + msg.getParameter();
        try {
            Thread.sleep(msg.getDuration());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(completionMessage);
        sender().tell(new JobCompletedMessage(msg.getKey(), completionMessage, msg.getName(), msg.getnOfFailures() + 1), self());
        System.err.println("Hi! The computation has completed! JobCompletedMessage sent!");

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
