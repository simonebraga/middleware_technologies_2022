package it.polimi.middleware.spark.processes;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.middleware.spark.utils.InputRecord;
import it.polimi.middleware.spark.utils.MiscUtils;
import it.polimi.middleware.spark.utils.POIMap;
import it.polimi.middleware.spark.utils.Position;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.OptionalDouble;

public class DataCleaningEnrichment {

    private static final POIMap poiMap = new POIMap();

    // Parse the input string into an actual object. Accepts input in the following form: {"pos":{"x":5.0,"y":7.0},"val":[10.0,11.0,12.0]}
    // INPUT: input_string
    // OUTPUT: <<x, y>, [values]>
    public static Tuple2<Position, ArrayList<Double>> parseInput(String in) {

        try {
            InputRecord record = MiscUtils.jacksonMapper.readValue(in, InputRecord.class);
            return new Tuple2<>(record.getPos(), record.getVal());
        } catch (JsonProcessingException e) {
            System.err.println("WARNING: Wrong format for input string. Record set to null.");
            return new Tuple2<>(null, null);
        }
    }

    private static boolean validateCoords(Position pos) {
        //TODO Implement a check on the value of the position
        return pos != null;
    }

    private static boolean validateReads(ArrayList<Double> reads) {
        //TODO Implement a check on the value of the reads
        return reads != null;
    }

    // Clean the RDD of non-valid tuples (null references, non-valid positions/readings)
    // During this step only the strong conditions are checked. Possible over-threshold values are checked later
    // INPUT: <<x,y>, [value]>
    // OUTPUT: boolean_outcome
    public static boolean cleanInput(Tuple2<Position, ArrayList<Double>> in) {
        return (validateCoords(in._1) && validateReads(in._2));
    }

    // Aggregate arrays with multiple readings into a single one (compute a clever average)
    // INPUT: <<x,y>, [values]>
    // OUTPUT: <<x,y>, value>
    public static Tuple2<Position, Double> aggregateReads(Tuple2<Position, ArrayList<Double>> in) {
        OptionalDouble average = in._2
                .stream()
                .mapToDouble(a -> a)
                .average();
        if (average.isPresent()) return new Tuple2<>(in._1, average.getAsDouble());
        return new Tuple2<>(in._1, null);
    }

    // Check if the aggregation produced a tuple with non-valid average (i.e. no valid values after filtering)
    // INPUT: <<x,y>, value>
    // OUTPUT: boolean_outcome
    public static boolean cleanAggregatedInput(Tuple2<Position, Double> in) {
        return in._2 != null;
    }

    // Convert from coordinates to POI ID using information about the map
    // INPUT: <<x,y>, value>
    // OUTPUT: <poi_id, value>
    public static Tuple2<String, Double> computePOI(Tuple2<Position, Double> in) {
        return new Tuple2<>(poiMap.getNearestPOI(in._1), in._2);
    }

    // At the end of those steps, tuples can be store for future analysis.
    // The "data-analysis" step needs still to be performed.
}
