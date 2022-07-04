package it.polimi.middlewareB.messages;

public class JobCompletedMessage {

    public JobCompletedMessage(String key, String notificationMessage) {
        this.key = key;
        this.notificationMessage = notificationMessage;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public String getKey() {
        return key;
    }

    private final String notificationMessage;
    private final String key;
}
