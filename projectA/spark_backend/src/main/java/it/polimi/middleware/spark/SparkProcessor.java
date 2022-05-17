package it.polimi.middleware.spark;

import it.polimi.middleware.spark.utils.MiscUtils;
import it.polimi.middleware.spark.utils.PoiMap;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.*;

import java.util.concurrent.TimeoutException;

import static it.polimi.middleware.spark.utils.PoiMap.computeNearestPoi;
import static org.apache.spark.sql.functions.*;

public class SparkProcessor {

    public static void main(String[] args) throws StreamingQueryException {

        MiscUtils.setLogLevel();

        // Use default values if not specified otherwise
        // Default values work in a local environment in IntelliJ
        final String master = args.length > 0 ? args[0] : "local[*]";
        final String bootstrap = args.length > 1 ? args[1] : "localhost:9092";
        final String topic = args.length > 2 ? args[2] : "rawNoise";
        final String q1_topic = args.length > 3 ? args[3] : "movingAverage";
        final String q2_topic = args.length > 4 ? args[4] : "top10poi";
        final String q3_topic = args.length > 5 ? args[5] : "noiseStreak";
        final String checkpointLocation = args.length > 6 ? args[6] : "/tmp/checkpoint";
        final String filePath = args.length > 7 ? args[7] : "src/main/resources/";
        final String fileName = args.length > 8 ? args[8] : "poi_map.json";

        PoiMap.initPoiMap(filePath + fileName);

        System.out.println("[LOG] Spark started with the following parameters:\n" +
                "[LOG] spark.master: " + master + "\n" +
                "[LOG] kafka.bootstrap.server: " + bootstrap + "\n" +
                "[LOG] checkpoint.location: " + checkpointLocation + "\n" +
                "[LOG] map.file.path: " + filePath + fileName);

        SparkSession spark = SparkSession
                .builder()
                .master(master)
                .config("spark.scheduler.mode", "FAIR") // Needed when running multi-thread queries
                .appName("SparkProcessor")
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
                .json(filePath + fileName + "l");

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
                .option("startingOffsets", "latest")
                .option("failOnDataLoss", "false") // This option is needed if Kafka is set up to drop events after some time, which is the case
                .load()
                .withWatermark("timestamp", "1 week") // Late events are discarded after 1 week (they are useless later than that)
                .selectExpr("CAST(value AS STRING)", "CAST(timestamp AS STRING)");

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

        // Substitute each pair of coordinates with the ID of the nearest POI
        UserDefinedFunction getNearestPoi = udf((Double x, Double y) -> computeNearestPoi(x,y), DataTypes.StringType);
        spark.udf().register("getNearestPoi", getNearestPoi);
        Dataset<Row> richDataset = cleanDataset
                .withColumn("poi_id", callUDF("getNearestPoi", col("x"), col("y")))
                .select("poi_id","val", "ts");

        /* END-SECTION */
        /* SECTION: Data analysis */

        // Q1: Hourly, daily, and weekly moving average of noise level, for each point of interest

        DataStreamWriter<Row> hourlyAverage = richDataset
                .groupBy(window(col("ts"), "1 hour", "1 hour"), col("poi_id"))
                .avg("val")
                .select(concat(
                        lit("avg_type: hourly, poi_id: "), //TODO Find a way to add col("window") instead of "avg_type: hourly"
                        col("poi_id"),
                        lit(", avg_value: "),
                        col("avg(val)")).as("value"))
                .selectExpr("CAST(value AS STRING)")
                .writeStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", bootstrap)
                .option("topic", q1_topic)
                .option("checkpointLocation", checkpointLocation + "/" + q1_topic + "/hourly") // Each query MUST have unique checkpoint location
                .outputMode("update");

        DataStreamWriter<Row> dailyAverage = richDataset
                .groupBy(window(col("ts"), "1 day", "1 day"), col("poi_id"))
                .avg("val")
                .select(concat(
                        lit("avg_type: daily, poi_id: "),
                        col("poi_id"),
                        lit(", avg_value: "),
                        col("avg(val)")).as("value"))
                .selectExpr("CAST(value AS STRING)")
                .writeStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", bootstrap)
                .option("topic", q1_topic)
                .option("checkpointLocation", checkpointLocation + "/" + q1_topic + "/daily")
                .outputMode("update");

        DataStreamWriter<Row> weeklyAverage = richDataset
                .groupBy(window(col("ts"), "1 week", "1 week"), col("poi_id"))
                .avg("val")
                .select(concat(
                        lit("avg_type: weekly, poi_id: "),
                        col("poi_id"),
                        lit(", avg_value: "),
                        col("avg(val)")).as("value"))
                .selectExpr("CAST(value AS STRING)")
                .writeStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", bootstrap)
                .option("topic", q1_topic)
                .option("checkpointLocation", checkpointLocation + "/" + q1_topic + "/weekly")
                .outputMode("update");

        // Q2: Top 10 points of interest with the highest level of noise over the last hour

        // Q3: Point of interest with the longest streak of good noise level

        /* END-SECTION */
        /* SECTION: Store results */

        new Thread(() -> {
            try {
                hourlyAverage.start();
            } catch (TimeoutException e) { System.err.println("[ERROR] Something went wrong executing \"hourlyAverage\" query!"); }
        }).start();

        new Thread(() -> {
            try {
                dailyAverage.start();
            } catch (TimeoutException e) { System.err.println("[ERROR] Something went wrong executing \"dailyAverage\" query!"); }
        }).start();

        new Thread(() -> {
            try {
                weeklyAverage.start();
            } catch (TimeoutException e) { System.err.println("[ERROR] Something went wrong executing \"weeklyAverage\" query!"); }
        }).start();

        // This condition should never be satisfied if using streaming queries (which is the case)
        spark.streams().awaitAnyTermination();

        /* END-SECTION */

        spark.close();
    }
}
