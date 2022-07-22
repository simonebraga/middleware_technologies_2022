package it.polimi.middlewareB.analysis;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.middlewareB.JSONJobDuration;
import it.polimi.middlewareB.messages.JobStatisticsMessage;
import it.polimi.middlewareB.messages.StartAnalysisMessage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RetriesAnalysisActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JobStatisticsMessage.class, this::echoRetriesStatistics)
                .match(StartAnalysisMessage.class, this::echoAnalysis)
                .build();
    }

    public void echoRetriesStatistics(JobStatisticsMessage msg){
        System.out.println(msg.getTimestamp() + " - Analysing " + msg.getName() + ", took " + msg.getnOfStarts() + " retries");
        buffer.addLast(msg);
    }

    private class JobCounts{
        //TODO are these access modifiers correct?
        // Is the private class enough to make its content private to RetriesAnalisisActor?
        public int nOfStarts = 0;
        public int nOfJobs = 0;
    }
    public void echoAnalysis(StartAnalysisMessage startMsg){
        LocalDateTime now = LocalDateTime.now();
        //clean queue for expired messages
            while (buffer.peekFirst() != null &&
                    ChronoUnit.MINUTES.between(buffer.peekFirst().getTimestamp(), now) >= 1) {
                buffer.pollFirst();
            }

        //process valid messages into map
        for (JobStatisticsMessage msg : buffer) {
            JobCounts counts = jobs.get(msg.getName());
            counts.nOfJobs++;
            counts.nOfStarts += msg.getnOfStarts();
        }

        System.out.println("----------------\n" +
                "Analysis of average starts:");
        for (Map.Entry<String, JobCounts> entry : jobs.entrySet()) {
            //prints analysis
            float average = (entry.getValue().nOfJobs != 0)?
                    ((float) entry.getValue().nOfStarts / entry.getValue().nOfJobs) :
                    0;
            System.out.println(entry.getKey() + ": " + average);
            //clean entry
            entry.getValue().nOfJobs = 0;
            entry.getValue().nOfStarts = 0;
        }
        System.out.println("----------------");

    }

    public static Props props(){
        return Props.create(RetriesAnalysisActor.class);
    }

    public RetriesAnalysisActor(){
        String JSONPathname = "src/main/resources/job_list.json";
        buffer = new ArrayDeque<>();
        try {
            jobs = populateMap(JSONPathname);
        } catch (IOException e) {
            System.err.println("Something went wrong when creating the map in RetriesAnalysisActor!");
            throw new RuntimeException(e);
        }
    }

    private Map<String, JobCounts> populateMap(String JSONFile) throws IOException {
        ObjectMapper jacksonMapper = new ObjectMapper();
        JSONJobDuration[] jobsArray;

        Map<String, JobCounts> jobsMap = new HashMap<>();

        try {
            jobsArray = jacksonMapper
                    .readValue(new File(JSONFile), JSONJobDuration[].class);
            for (JSONJobDuration jt : jobsArray) {
                jobsMap.put(jt.getName(), new JobCounts());
            }
        } catch (JsonMappingException e) {
            System.err.println("[ERROR] Fatal problem with mapping of map file content!");
        } catch (JsonParseException e) {
            System.err.println("[ERROR] Map file is bad formed!");
        } catch (IOException e) {
            System.err.println("IOException reading json file!");
            throw new RuntimeException(e);
        }
        if (jobsMap.isEmpty()){
            throw new IOException("Something went wrong in parsing the JSON file!");
        }
        return jobsMap;
    }
    private ArrayDeque<JobStatisticsMessage> buffer;
    private Map<String, JobCounts> jobs;
}
