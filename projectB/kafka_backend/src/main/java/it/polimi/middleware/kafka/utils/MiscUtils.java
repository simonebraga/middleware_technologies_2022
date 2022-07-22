package it.polimi.middleware.kafka.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MiscUtils {

    // This mapper object is accessible everywhere
    public static final ObjectMapper jacksonMapper = new ObjectMapper();
}
