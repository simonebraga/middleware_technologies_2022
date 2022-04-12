package it.polimi.middleware.spark.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

// This code is partially copied from the exercise sessions

public class MiscUtils {

    public static final ObjectMapper jacksonMapper = new ObjectMapper();

    public static void setLogLevel() {
        Logger.getLogger("org").setLevel(Level.OFF);
    }

}