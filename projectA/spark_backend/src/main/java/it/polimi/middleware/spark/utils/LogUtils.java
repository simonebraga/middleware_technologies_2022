package it.polimi.middleware.spark.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

// This code is copied from the exercise sessions

public class LogUtils {

    public static void setLogLevel() {
        Logger.getLogger("org").setLevel(Level.OFF);
    }

}