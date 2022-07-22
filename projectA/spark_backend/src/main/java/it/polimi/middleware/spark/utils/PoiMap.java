package it.polimi.middleware.spark.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class PoiMap implements Serializable {

    private Poi[] poiList;

    // Initialize the set of POIs
    // If the file is bad formed or can't be found, initialize the map with a single poi to avoid errors later
    public void initPoiMap(String mapFile) {

        poiList = new Poi[1];
        poiList[0] = new Poi("NULL_POI", 0.0, 0.0);

        try {
            poiList = MiscUtils.jacksonMapper.readValue(new File(mapFile), Poi[].class);
        } catch (JsonMappingException e) {
            System.err.println("[ERROR] Fatal problem with mapping of map file content!");
        } catch (JsonParseException e) {
            System.err.println("[ERROR] Map file is bad formed!");
        } catch (IOException e) {
            System.err.println("[ERROR] Map file cannot be found!");
        }
    }

    // Compute the distance between given coordinates and a given POI
    private Double computeDistance(Double x, Double y, Poi poi) {
        Double dx = x - poi.getX();
        Double dy = y - poi.getY();
        return Math.sqrt(dx*dx + dy*dy);
    }

    // Retrieve the ID of the nearest POI for given coordinates
    public String computeNearestPoi(Double x, Double y) {

        double min = computeDistance(x, y, poiList[0]);
        String id = poiList[0].getId();

        for (Poi poi:poiList) {
            Double newDist = computeDistance(x, y, poi);
            if (newDist < min) {
                min = newDist;
                id = poi.getId();
            }
        }

        return id;
    }
}