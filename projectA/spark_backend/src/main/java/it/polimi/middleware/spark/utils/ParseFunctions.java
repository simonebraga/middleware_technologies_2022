package it.polimi.middleware.spark.utils;

import scala.Tuple2;
import scala.Tuple3;

// This class contains the functions that allow the computation for the requested metrics

public class ParseFunctions {

    public static Tuple2<String, Float> parseInput(String in) {
        //TODO Actual implementation of the parsing
        return new Tuple2<>("coords", Float.parseFloat(in));
    }

    public static Tuple2<String, Float> computePOI(Tuple2<String, Float> in) {
        //TODO Actual conversion from coordinates to POI using information about the map
        return new Tuple2<>("poi_id", in._2);
    }

    // Just extend every tuple with a 1 that will be used in the reduction
    public static Tuple3<String, Float, Integer> extendTuple(Tuple2<String, Float> in) {
        return new Tuple3<>(in._1, in._2, 1);
    }

    public static Tuple3<String, Float, Integer> reduceOverPOI(Tuple3<String, Float, Integer> a, Tuple3<String, Float, Integer> b) {
        //TODO Actual reduction over _1 performing sum of _2 and _3
        return new Tuple3<>("", 1.0F, 1);
    }

    // Compute the mean for every POI
    public static Tuple2<String, Float> computeMean(Tuple3<String, Float, Integer> in) {
        return new Tuple2<>(in._1(), in._2() / in._3().floatValue());
    }
}
