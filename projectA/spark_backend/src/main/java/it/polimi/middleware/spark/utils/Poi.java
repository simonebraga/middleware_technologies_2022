package it.polimi.middleware.spark.utils;

import java.io.Serializable;

// A Point Of Interest (POI) is a set of 3 variables:
// id -> Identifier (ID) of the POI
// x -> Horizontal coordinate of the POI
// y -> Vertical coordinate of the POI

public class Poi implements Serializable {

    private String id;
    private Double x;
    private Double y;

    public Poi() {
    }

    public Poi(String id, Double x, Double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
