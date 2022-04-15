package it.polimi.middleware.spark.utils;

import scala.Tuple2;

//TODO Move all the methods of this class in the "processes" package

public class ParseFunctions {

    // Rebuild every tuple to allow application of "reduction by key"
    // INPUT: <poi_id, value>
    // OUTPUT: <poi_id, <value, 1>>
    public static Tuple2<String, Tuple2<Float, Integer>> rebuildTuple(Tuple2<String, Float> in) {
        return new Tuple2<>(in._1, new Tuple2<>(in._2, 1));
    }

    // Reduce two tuples with the same POI
    // INPUT: <value1, #> <value2, #>
    // OUTPUT: <tot_value, cardinality>
    public static Tuple2<Float, Integer> reduceOverPOI(Tuple2<Float, Integer> a, Tuple2<Float, Integer> b) {
        return new Tuple2<>(a._1 + b._1, a._2 + b._2);
    }

    // Compute the mean value for every POI
    // INPUT: <poi_id, tot_value, cardinality>
    // OUTPUT: <poi_id, mean>
    public static Tuple2<String, Float> computeMean(Tuple2<String, Tuple2<Float, Integer>> in) {
        return new Tuple2<>(in._1, in._2._1 / in._2._2.floatValue());
    }
}
