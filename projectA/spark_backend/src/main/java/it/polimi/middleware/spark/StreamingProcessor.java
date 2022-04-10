package it.polimi.middleware.spark;

import it.polimi.middleware.spark.utils.LogUtils;
import it.polimi.middleware.spark.utils.ParseFunctions;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

public class StreamingProcessor {

    public static void main(String[] args) {

        LogUtils.setLogLevel();

        // If not specified otherwise, use local instance of Spark with all available threads
        final String master = args.length > 0 ? args[0] : "local[*]";

        // Setup StreamingContext with batch interval of 1 second
        //TODO Check if 1 second interval is ok
        final SparkConf conf = new SparkConf().setMaster(master).setAppName("StreamingProcessorStats");
        final JavaStreamingContext sc = new JavaStreamingContext(conf, Durations.seconds(1));

        // Perform the union of multiple sockets inputs
        //TODO Make it dynamic w.r.t. connected clients (for now, it accepts up to 4 connections)
        final JavaDStream<String> collector = sc.socketTextStream("localhost", 9999)
                .union(sc.socketTextStream("localhost", 9998))
                .union(sc.socketTextStream("localhost", 9997))
                .union(sc.socketTextStream("localhost", 9996));

        // Create a DStream that uses "collector" object as a source
        final JavaDStream<Tuple2<String, Float>> values = collector
                .window(Durations.seconds(10), Durations.seconds(5))
                .map(ParseFunctions::parseInput)
                .map(ParseFunctions::computePOI)
                .map(ParseFunctions::extendTuple)
                .reduce(ParseFunctions::reduceOverPOI)
                .map(ParseFunctions::computeMean);

        //TODO Implement a fancy print for the computed metrics
        values.print();

        // Start the computation
        sc.start();

        try {
            sc.awaitTermination();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        // Wait for the computation to terminate
        sc.close();
    }
}
