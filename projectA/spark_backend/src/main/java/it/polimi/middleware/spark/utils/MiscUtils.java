package it.polimi.middleware.spark.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MiscUtils {

    // This mapper object is accessible everywhere
    public static final ObjectMapper jacksonMapper = new ObjectMapper();

    // Copied from the exercise sessions
    public static void setLogLevel() {
        Logger.getLogger("org").setLevel(Level.OFF);
    }
}