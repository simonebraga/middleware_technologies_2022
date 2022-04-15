package it.polimi.middleware.spark.utils;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.io.IOException;

// This class contains the initialized set of POIs and the methods to manipulate them
public class POIMap {

    //TODO Allow to set file path from command line
    private static final String mapFileName = "src/main/resources/poi_map.json";

    private POI[] poi_list;

    // Initialize the set of POIs.
    // If the file is bad formed or can't be found, initialize the map with a single poi to avoid errors later.
    public POIMap() {
        try {
            poi_list = MiscUtils.jacksonMapper.readValue(new File(mapFileName), POI[].class);
        } catch (JsonProcessingException e) {
            System.err.println("ERROR: Map file is bad formed!");
            poi_list = new POI[1];
            poi_list[0] = new POI("NULL_POI", 0.0, 0.0);
        } catch (IOException e) {
            System.err.println("ERROR: Map file cannot be found!");
            poi_list = new POI[1];
            poi_list[0] = new POI("NULL_POI", 0.0, 0.0);
        }
    }

    // Compute the distance between a given POI and a given position
    private Double computeDistance(POI poi, Position pos) {
        Double dx = poi.getX() - pos.getX();
        Double dy = poi.getY() - pos.getY();
        return Math.sqrt(dx*dx + dy*dy);
    }

    // Find the ID of the nearest POI for a given position
    public String getNearestPOI(Position pos) {

        double min = computeDistance(poi_list[0], pos);
        String ret_val = poi_list[0].getId();

        for (POI poi:poi_list) {
            Double new_dist = computeDistance(poi, pos);
            if (new_dist < min) {
                min = new_dist;
                ret_val = poi.getId();
            }
        }

        return ret_val;
    }
}
