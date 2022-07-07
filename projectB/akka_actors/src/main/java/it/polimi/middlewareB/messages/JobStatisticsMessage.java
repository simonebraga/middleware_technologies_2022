package it.polimi.middlewareB.messages;

import java.time.LocalDateTime;

public class JobStatisticsMessage {
    public JobStatisticsMessage(String name, int nOfStarts){
        this.name = name;
        this.nOfStarts = nOfStarts;
        this.timestamp = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public int getnOfStarts() {
        return nOfStarts;
    }

    public LocalDateTime getTimestamp(){
        return timestamp;
    }

    private String name;
    private int nOfStarts;
    private LocalDateTime timestamp;
}
