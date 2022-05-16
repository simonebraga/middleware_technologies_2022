package it.polimi.middleware.spark;

import it.polimi.middleware.spark.utils.MiscUtils;
import it.polimi.middleware.spark.utils.PoiMap;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.*;

import java.util.concurrent.TimeoutException;

import static it.polimi.middleware.spark.utils.PoiMap.computeNearestPoi;
import static org.apache.spark.sql.functions.*;

public class SparkProcessor {

    public static void main(String[] args) throws TimeoutException, StreamingQueryException {

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
                .appName("SparkProcessor")
                .getOrCreate();

        /* SECTION: POI dataset initialization */

        // Define the schema for the POIs dataset
        StructType poiSchema = DataTypes.createStructType(new StructField[] {
                DataTypes.createStructField("id", DataTypes.StringType, false),
                DataTypes.createStructField("x", DataTypes.DoubleType, false),
                DataTypes.createStructField("y", DataTypes.DoubleType, false),
        });

        // Create an instance of a "relatively small dataset" of POIs
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
                .withWatermark("timestamp", "1 week") // Late events are discarded after 1 week
                .selectExpr("CAST(value AS STRING)", "CAST(timestamp AS STRING)");

        // Clean the rows received from Kafka and apply the schema to the "value" field
        Dataset<Row> rawDataset = rawRecord
                .select(from_json(col("value"), rawSchema).as("data"), col("timestamp").as("ts"))
                .select("data.*", "ts");

        /* END-SECTION */
        /* SECTION: Data cleaning and enrichment */

        Dataset<Row> cleanDataset = rawDataset.na().drop();//.select("x", "y", "val", "ts");

        //TODO Fix this transformation to use only Spark SQL

        // Substitute each pair of coordinates with the ID of the nearest POI
        UserDefinedFunction getNearestPoi = udf((Double x, Double y) -> computeNearestPoi(x,y), DataTypes.StringType);
        spark.udf().register("getNearestPoi", getNearestPoi);
        Dataset<Row> richDataset = cleanDataset
                .withColumn("poi_id", callUDF("getNearestPoi", col("x"), col("y")))
                .select("poi_id","val", "ts");

        /* END-SECTION */
        /* SECTION: Data analysis */

        //TODO Enhance data analysis

        // Q1: Hourly, daily, and weekly moving average of noise level, for each point of interest

        StreamingQuery hourlyAverage = richDataset
                .groupBy(
                        window(col("ts"), "1 hour", "1 hour"),
                        col("poi_id"))
                .avg("val")
                .select(concat(col("poi_id"), lit(": "),col("avg(val)")).as("value"))
                .selectExpr("CAST(value AS STRING)")
                .writeStream()
                .format("kafka")
                .option("checkpointLocation", checkpointLocation)
                .option("kafka.bootstrap.servers", bootstrap)
                .option("topic", q1_topic)
                .outputMode("update")
                .start();

        // Q2: Top 10 points of interest with the highest level of noise over the last hour

        // Q3: Point of interest with the longest streak of good noise level

        /* END-SECTION */
        /* SECTION: Store results */

        //TODO Store results in a Kafka topic

        hourlyAverage.awaitTermination();
        //TODO Remove in final release

        /* richDataset
                .writeStream()
                .format("console")
                .outputMode("append")
                .option("truncate", "false")
                .start()
                .awaitTermination(); */

        /* END-SECTION */

        spark.close();
    }
}
