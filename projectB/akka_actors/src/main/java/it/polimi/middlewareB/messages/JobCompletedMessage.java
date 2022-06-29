package it.polimi.middlewareB.messages;

public class JobCompletedMessage {

    public JobCompletedMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    private String notificationMessage;
}
