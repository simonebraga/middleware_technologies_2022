package it.polimi.middleware.kafka.utils;

import java.io.Serializable;

public class Job implements Serializable {

    private String id;
    private String param_name;

    public Job() {
    }

    public Job(String id, String param_name) {
        this.id = id;
        this.param_name = param_name;
    }

    public String getId() {
        return id;
    }

    public String getParam_name() {
        return param_name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setParam_name(String param_name) {
        this.param_name = param_name;
    }
}
