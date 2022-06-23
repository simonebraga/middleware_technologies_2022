package it.polimi.middleware.kafka.utils;

import java.util.ArrayList;

public class NotificationList {

    private final ArrayList<String> notificationList = new ArrayList<>();

    // Adds new key to the notification list only if not already present
    public synchronized boolean add(String key) {
        if (notificationList.contains(key))
            return false;
        notificationList.add(key);
        return true;
    }

    // Remove all the occurrences of key from the notification list (there shouldn't already be any)
    public synchronized boolean checkRemove(String key) {
        boolean outcome = false;
        while (notificationList.remove(key)) {
            outcome = true;
        }
        return outcome;
    }

    public synchronized String[] get() {
        return notificationList.toArray(new String[0]);
    }
}
