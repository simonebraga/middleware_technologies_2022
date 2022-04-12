package it.polimi.middleware.spark;

import it.polimi.middleware.spark.utils.MiscUtils;
import it.polimi.middleware.spark.utils.ParseFunctions;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

public class StreamingProcessor {

    public static void main(String[] args) {

        MiscUtils.setLogLevel();

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
        //TODO Check if window's duration is ok
        final JavaDStream<Tuple2<String, Float>> mean = collector
                .window(Durations.seconds(30), Durations.seconds(5))
                .map(ParseFunctions::parseInput)
                .filter(ParseFunctions::cleanInput)
                .map(ParseFunctions::computePOI)
                .map(ParseFunctions::rebuildTuple)
                .mapToPair(q -> new Tuple2<>(q._1, q._2))
                .reduceByKey(ParseFunctions::reduceOverPOI)
                .map(ParseFunctions::computeMean);

        //TODO Implement a fancy print for the computed metrics
        mean.print();

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
