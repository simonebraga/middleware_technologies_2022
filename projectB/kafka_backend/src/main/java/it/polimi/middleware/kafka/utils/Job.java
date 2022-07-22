package it.polimi.middleware.kafka.utils;

import java.io.Serializable;

public class Job implements Serializable {

    private String name;
    private String param_name;

    public Job() {
    }

    public Job(String id, String param_name) {
        this.name = id;
        this.param_name = param_name;
    }

    public String getName() {
        return name;
    }

    public String getParam_name() {
        return param_name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParam_name(String param_name) {
        this.param_name = param_name;
    }
}
