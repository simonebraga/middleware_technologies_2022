package it.polimi.middleware.kafka.utils;

import java.io.Serializable;

public class Job implements Serializable {

    private String name;
    private int duration;

    public Job() {
    }

    public Job(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Job{" +
                "name='" + name + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
