package it.polimi.middleware.spark.utils;

import java.util.ArrayList;

public class InputRecord {

    private Position pos;
    private ArrayList<Double> val;

    public InputRecord() {
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public void setVal(ArrayList<Double> val) {
        this.val = val;
    }

    public Position getPos() {
        return pos;
    }

    public ArrayList<Double> getVal() {
        return val;
    }
}
