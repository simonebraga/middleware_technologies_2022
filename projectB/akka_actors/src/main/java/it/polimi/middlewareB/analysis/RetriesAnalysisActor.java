package it.polimi.middlewareB.analysis;

import akka.actor.AbstractActor;
import akka.actor.Props;
import it.polimi.middlewareB.messages.JobStatisticsMessage;

public class RetriesAnalysisActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JobStatisticsMessage.class, this::echoRetriesStatistics)
                .build();
    }

    public void echoRetriesStatistics(JobStatisticsMessage msg){
        System.out.println(msg.getTimestamp() + " - Analysing " + msg.getName() + ", took " + msg.getnOfStarts() + " retries");
    }

    public static Props props(){
        return Props.create(RetriesAnalysisActor.class);
    }
}
