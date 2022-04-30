package it.polimi.middleware.spark.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MiscUtils {

    // This code is copied from the exercise sessions
    public static void setLogLevel() {
        Logger.getLogger("org").setLevel(Level.OFF);
    }
}