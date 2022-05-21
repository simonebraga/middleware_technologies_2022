package it.polimi.middleware.spark;

import it.polimi.middleware.spark.utils.MiscUtils;
import it.polimi.middleware.spark.utils.PoiMap;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.*;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

public class CleaningEnrichment {

    public static void main(String[] args) throws StreamingQueryException {

        MiscUtils.setLogLevel();

        // Use default values if not specified otherwise
        // Default values work in a local environment in IntelliJ
        final String master = args.length > 0 ? args[0] : "local[*]";
        final String bootstrap = args.length > 1 ? args[1] : "localhost:9092";
        final String topic = args.length > 2 ? args[2] : "rawNoise";
        final String out_topic = args.length > 3 ? args[3] : "richNoise";
        final String checkpointLocation = args.length > 4 ? args[4] : "/tmp/cleaning_enrichment/checkpoint";
        final String noiseDataLocation = args.length > 5 ? args[5] : "/tmp/cleaning_enrichment/noise_data";
        final String mapFilePath = args.length > 6 ? args[6] : "src/main/resources/";
        final String mapFileName = args.length > 7 ? args[7] : "poi_map.json";

        PoiMap.initPoiMap(mapFilePath + mapFileName);

        System.out.println("[LOG] Spark started with the following parameters:\n" +
                "[LOG] spark.master: " + master + "\n" +
                "[LOG] kafka.bootstrap.server: " + bootstrap + "\n" +
                "[LOG] checkpoint.location: " + checkpointLocation + "\n" +
                "[LOG] noise.data.location: " + noiseDataLocation + "\n" +
                "[LOG] map.file.path: " + mapFilePath + mapFileName);

        SparkSession spark = SparkSession
                .builder()
                .master(master)
                .config("spark.scheduler.mode", "FAIR") // Needed when running multi-thread queries
                .appName("CleaningEnrichment")
                .getOrCreate();

        /* SECTION: POI dataset initialization */

        // Define the schema for the POIs dataset
        StructType poiSchema = DataTypes.createStructType(new StructField[] {
                DataTypes.createStructField("id", DataTypes.StringType, false),
                DataTypes.createStructField("x", DataTypes.DoubleType, false),
                DataTypes.createStructField("y", DataTypes.DoubleType, false),
        });

        // Create an instance of a static "relatively small dataset" of POIs
        Dataset<Row> poiDataset = spark
                .read()
                .schema(poiSchema)
                .json(mapFilePath + mapFileName + "l");

        /* END-SECTION */
        /* SECTION: Streaming table initialization */

        // Define the schema for each string coming from external applications
        // This schema only applies to "value" field of Kafka events
        StructType rawSchema = DataTypes.createStructType(new StructField[] {
                DataTypes.createStructField("x", DataTypes.DoubleType, true),
                DataTypes.createStructField("y", DataTypes.DoubleType, true),
                DataTypes.createStructField("val", DataTypes.DoubleType, true),
        });

        // Create a row for each Kafka event
        // "value" and "timestamp" fields of each row are parsed as strings to be used later on
        Dataset<Row> rawRecord = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", bootstrap)
                .option("subscribe", topic)
                .option("startingOffsets", "latest") // "earliest" not needed, non-processed events are automatically retrieved
                .option("failOnDataLoss", "false") // This option is needed if Kafka is set up to drop events after some time
                .load()
                .withWatermark("timestamp", "1 week") // Late events are discarded after 1 week (they are useless later than that)
                .selectExpr("CAST(value AS STRING)", "CAST(timestamp AS TIMESTAMP)");

        // Clean the rows received from Kafka and apply the schema to the "value" field
        Dataset<Row> rawDataset = rawRecord
                .select(from_json(col("value"), rawSchema).as("data"), col("timestamp").as("ts"))
                .select("data.*", "ts");

        /* END-SECTION */
        /* SECTION: Data cleaning and enrichment */

        //TODO Enhance data cleaning

        // Remove records with null or out-of-range fields
        Dataset<Row> cleanDataset = rawDataset.na().drop();

        //TODO Perform this with Spark SQL

        //TODO Broadcast ObjectMapper to every worker

        // Substitute each pair of coordinates with the ID of the nearest POI
        UserDefinedFunction getNearestPoi = udf((Double x, Double y) -> PoiMap.computeNearestPoi(x,y), DataTypes.StringType);
        spark.udf().register("getNearestPoi", getNearestPoi);
        Dataset<Row> richDataset = cleanDataset
                .withColumn("poi_id", callUDF("getNearestPoi", col("x"), col("y")))
                .select("poi_id","val", "ts");

        /* END-SECTION */
        /* SECTION: Store results */

        // Create one stream writer for each sink

        DataStreamWriter<Row> fileOutput = richDataset
                .select("poi_id", "val", "ts")
                .withColumn("batch_ts", current_timestamp())
                .writeStream()
                .format("csv")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .option("path", noiseDataLocation)
                .option("checkpointLocation", checkpointLocation + "/file")
                .option("header", true)
                .option("timestampFormat", "yyyy-MM-dd HH:mm:ss.SSSSSS") // Must be the same format of current_timestamp()
                .partitionBy("batch_ts")
                .outputMode("append");

        DataStreamWriter<Row> kafkaOutput = richDataset
                .select(concat(
                        lit("{\"poi_id\":\""),
                        col("poi_id"),
                        lit("\",\"val\":"),
                        col("val"),
                        lit(",\"ts\":\""),
                        col("ts"),
                        lit("\"}")
                ).as("value"))
                .selectExpr("CAST(value AS STRING)")
                .writeStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", bootstrap)
                .option("topic", out_topic)
                .option("checkpointLocation", checkpointLocation + "/kafka")
                .outputMode("append");

        // Parallelize the streams already created to store results to all the sinks

        new Thread(() -> {
            try {
                fileOutput.start();
            } catch (TimeoutException e) {
                System.err.println("[ERROR] Something went wrong with the file sink!");
            }
        }).start();

        new Thread(() -> {
            try {
                kafkaOutput.start();
            } catch (TimeoutException e) {
                System.err.println("[ERROR] Something went wrong with the Kafka sink!");
            }
        }).start();

        System.out.println("[LOG] Cleaning and enrichment process started.\nType \"quit\" to stop the process");
        Scanner in = new Scanner(System.in);
        while (!in.nextLine().equals("quit")) {
            System.out.println("Unknown command. Type \"quit\" to stop the application.");
        }

        // As an alternative termination condition the following code can be used
        // spark.streams().awaitAnyTermination();

        /* END-SECTION */

        spark.close();
    }
}
