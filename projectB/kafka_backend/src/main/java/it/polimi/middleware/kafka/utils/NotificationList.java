package it.polimi.middleware.kafka.utils;

import java.util.ArrayList;

public class NotificationList {

    private final ArrayList<String> notificationList = new ArrayList<>();

    public synchronized void add(String key) {
        notificationList.add(key);
    }

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
