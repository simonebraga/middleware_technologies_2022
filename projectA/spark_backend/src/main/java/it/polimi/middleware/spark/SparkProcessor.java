package it.polimi.middleware.spark;

import it.polimi.middleware.spark.utils.MiscUtils;
import it.polimi.middleware.spark.utils.PoiMap;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
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
        final String q1h_topic = args.length > 3 ? args[3] : "hourlyAverage";
        final String q1d_topic = args.length > 4 ? args[4] : "dailyAverage";
        final String q1w_topic = args.length > 5 ? args[5] : "weeklyAverage";
        final String q2_topic = args.length > 6 ? args[6] : "top10poi";
        final String q3_topic = args.length > 7 ? args[7] : "noiseStreak";
        final Double q3_threshold = args.length > 8 ? Double.parseDouble(args[8]) : 100.0;
        final String checkpointLocation = args.length > 9 ? args[9] : "/tmp/checkpoint";
        final String filePath = args.length > 10 ? args[10] : "src/main/resources/";
        final String fileName = args.length > 11 ? args[11] : "poi_map.json";

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
                .option("startingOffsets", "earliest") // "earliest" option is needed for Q2 and Q3
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

        // Substitute each pair of coordinates with the ID of the nearest POI
        UserDefinedFunction getNearestPoi = udf((Double x, Double y) -> computeNearestPoi(x,y), DataTypes.StringType);
        spark.udf().register("getNearestPoi", getNearestPoi);
        Dataset<Row> richDataset = cleanDataset
                .withColumn("poi_id", callUDF("getNearestPoi", col("x"), col("y")))
                .select("poi_id","val", "ts");

        /* END-SECTION */
        /* SECTION: Data analysis */

        // Q1: Hourly, daily, and weekly moving average of noise level, for each point of interest
        // Implementation: Aggregate rows by columns "window" and "poi_id", compute the average of column "value"

        DataStreamWriter<Row> hourlyAverage = richDataset
                .groupBy(window(col("ts"), "1 hour", "1 hour"), col("poi_id"))
                .avg("val")
                .select(concat(
                        lit("{\"window\":"),
                        col("window").cast(DataTypes.StringType),
                        lit(",\"poi_id\":\""),
                        col("poi_id"),
                        lit("\",\"avg_value\":"),
                        col("avg(val)").cast(DataTypes.StringType),
                        lit("}")).as("value"))
                .selectExpr("CAST(value AS STRING)")
                .writeStream()
                .format("kafka")
                //.trigger(Trigger.ProcessingTime("1 minute")) // Set batch interval to slow down result population
                .option("kafka.bootstrap.servers", bootstrap)
                .option("topic", q1h_topic)
                .option("checkpointLocation", checkpointLocation + "/" + q1h_topic) // Each query MUST have unique checkpoint location
                .outputMode("update");

        DataStreamWriter<Row> dailyAverage = richDataset
                .groupBy(window(col("ts"), "1 day", "1 day"), col("poi_id"))
                .avg("val")
                .select(concat(
                        lit("{\"window\":"),
                        col("window").cast(DataTypes.StringType),
                        lit(",\"poi_id\":\""),
                        col("poi_id"),
                        lit("\",\"avg_value\":"),
                        col("avg(val)").cast(DataTypes.StringType),
                        lit("}")).as("value"))
                .selectExpr("CAST(value AS STRING)")
                .writeStream()
                .format("kafka")
                //.trigger(Trigger.ProcessingTime("1 minute")) // Set batch interval to slow down result population
                .option("kafka.bootstrap.servers", bootstrap)
                .option("topic", q1d_topic)
                .option("checkpointLocation", checkpointLocation + "/" + q1d_topic)
                .outputMode("update");

        DataStreamWriter<Row> weeklyAverage = richDataset
                .groupBy(window(col("ts"), "1 week", "1 week"), col("poi_id"))
                .avg("val")
                .select(concat(
                        lit("{\"window\":"),
                        col("window").cast(DataTypes.StringType),
                        lit(",\"poi_id\":\""),
                        col("poi_id"),
                        lit("\",\"avg_value\":"),
                        col("avg(val)").cast(DataTypes.StringType),
                        lit("}")).as("value"))
                .selectExpr("CAST(value AS STRING)")
                .writeStream()
                .format("kafka")
                //.trigger(Trigger.ProcessingTime("1 minute")) // Set batch interval to slow down result population
                .option("kafka.bootstrap.servers", bootstrap)
                .option("topic", q1w_topic)
                .option("checkpointLocation", checkpointLocation + "/" + q1w_topic)
                .outputMode("update");

        // Q2: Top 10 points of interest with the highest level of noise over the last hour
        // Implementation: Perform the same query as Q1, then order by descending column "window" and column "avg(val)"

        //TODO Select only rows of interest (not possible in Spark structured streaming)

        DataStreamWriter<Row> top10poi = richDataset
                .groupBy(window(col("ts"), "1 hour", "1 hour"), col("poi_id"))
                .avg("val")
                .orderBy(desc("window"), desc("avg(val)"))
                .select(concat(
                        lit("{\"window\":"),
                        col("window").cast(DataTypes.StringType),
                        lit(",\"poi_id\":\""),
                        col("poi_id"),
                        lit("\",\"avg_value\":"),
                        col("avg(val)").cast(DataTypes.StringType),
                        lit("}")).as("value"))
                .selectExpr("CAST(value AS STRING)")
                .writeStream()
                .format("kafka")
                //.trigger(Trigger.ProcessingTime("1 minute")) // Set batch interval to slow down result population
                .option("kafka.bootstrap.servers", bootstrap)
                .option("topic", q2_topic)
                .option("checkpointLocation", checkpointLocation + "/" + q2_topic)
                .outputMode("complete");

        // Q3: Point of interest with the longest streak of good noise level
        // Implementation: For each POI, select the last over-threshold value, compute the new column "streak" then order by descending column "streak"

        //TODO Select only rows of interest (not possible in Spark structured streaming)

        DataStreamWriter<Row> noiseStreak = richDataset
                .where(col("val").gt(q3_threshold))
                .groupBy(col("poi_id"))
                .agg(max(col("ts")))
                .withColumn("streak", current_timestamp().minus(col("max(ts)")))
                .orderBy(desc("streak"))
                .select(concat(
                        lit("{\"starting_timestamp\":{"),
                        col("max(ts)").cast(DataTypes.StringType),
                        lit("},\"poi_id\":\""),
                        col("poi_id"),
                        lit("\",\"streak\":{"),
                        col("streak").cast(DataTypes.StringType),
                        lit("}}")).as("value"))
                .selectExpr("CAST(value AS STRING)")
                .writeStream()
                .format("kafka")
                //.trigger(Trigger.ProcessingTime("1 minute")) // Set batch interval to slow down result population
                .option("kafka.bootstrap.servers", bootstrap)
                .option("topic", q3_topic)
                .option("checkpointLocation", checkpointLocation + "/" + q3_topic)
                .outputMode("complete");

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

        new Thread(() -> {
            try {
                top10poi.start();
            } catch (TimeoutException e) { System.err.println("[ERROR] Something went wrong executing \"top10poi\" query!"); }
        }).start();

        new Thread(() -> {
            try {
                noiseStreak.start();
            } catch (TimeoutException e) { System.err.println("[ERROR] Something went wrong executing \"noiseStreak\" query!"); }
        }).start();

        // This condition should never be satisfied if using streaming queries (which is the case)
        spark.streams().awaitAnyTermination();

        /* END-SECTION */

        spark.close();
    }
}
