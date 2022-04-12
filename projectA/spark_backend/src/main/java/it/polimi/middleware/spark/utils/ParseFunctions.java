package it.polimi.middleware.spark.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import scala.Tuple2;

// Serialized in the following form: {"coords":"10;24","val":12.142}
class Record {
    public String coords;
    public Float val;
}

// This class contains the functions that allow the computation for the requested metrics
public class ParseFunctions {

    // Parse the input string to actual values
    // INPUT: input_record
    // OUTPUT: <coords, value>
    public static Tuple2<String, Float> parseInput(String in) {
        try {
            Record rec = MiscUtils.jacksonMapper.readValue(in, Record.class);
            return new Tuple2<>(rec.coords, rec.val);
        } catch (JsonProcessingException e) {
            System.err.println("WARNING: Wrong format for input string. Record set to null.");
            return new Tuple2<>(null, null);
        }
    }

    // Clean the RDD of null tuples
    // INPUT: <coords, value>
    // OUTPUT: boolean_outcome
    public static boolean cleanInput(Tuple2<String, Float> in) {
        return (in._1 != null) && (in._2 != null);
    }

    // Convert from coordinates to POI using information about the map
    // INPUT: <coords, value>
    // OUTPUT: <poi_id, value>
    public static Tuple2<String, Float> computePOI(Tuple2<String, Float> in) {
        //TODO For now, it uses the coordinates as an ID
        return new Tuple2<>(in._1, in._2);
    }

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
