package it.polimi.middlewareB.messages;

public class JobCompletedMessage {

    public JobCompletedMessage(String key, String notificationMessage, String name, int nOfStarts) {
        this.key = key;
        this.notificationMessage = notificationMessage;
        this.name = name;
        this.nOfStarts = nOfStarts;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public String getKey() {
        return key;
    }

    public int getnOfStarts(){
        return nOfStarts;
    }

    public String getName(){
        return name;
    }

    //TODO is this redundant with this.name?
    private final String notificationMessage;
    private final String key;
    private final String name;
    private final int nOfStarts;
}
