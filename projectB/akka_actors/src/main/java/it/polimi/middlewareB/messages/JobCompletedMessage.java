package it.polimi.middlewareB.messages;

public class JobCompletedMessage {

    public JobCompletedMessage(String key, String notificationMessage, int nOfStarts) {
        this.key = key;
        this.notificationMessage = notificationMessage;
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

    private final String notificationMessage;
    private final String key;
    private final int nOfStarts;
}
