package it.polimi.middleware.kafka.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class JobList implements Serializable {

    private Job[] jobList;

    // Initialize the set of jobs
    // If the file is bad formed or can't be found, initialize the list with a single job to avoid errors later
    public void initJobList(String jobFile) {

        jobList = new Job[1];
        jobList[0] = new Job("job-mockup", "PARAMETER_MOCKUP");

        try {
            jobList = MiscUtils.jacksonMapper.readValue(new File(jobFile), Job[].class);
        } catch (JsonMappingException e) {
            System.err.println("[ERROR] Fatal problem with mapping of job file content!");
        } catch (JsonParseException e) {
            System.err.println("[ERROR] Job file is bad formed!");
        } catch (IOException e) {
            System.err.println("[ERROR] Job file cannot be found!");
        }
    }

    public Job[] getJobList() {
        return jobList;
    }

    // Return an ArrayList of strings containing the ID of the jobs (shall be used for fast lookup)
    public ArrayList<String> getJobArrayList() {
        String[] jobIDList = new String[jobList.length];
        for (int i = 0; i < jobList.length; i++)
            jobIDList[i] = jobList[i].getName();
        return new ArrayList<>(Arrays.asList(jobIDList));
    }
}
