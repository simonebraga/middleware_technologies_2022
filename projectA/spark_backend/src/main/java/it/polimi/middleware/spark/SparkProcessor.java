package it.polimi.middleware.spark;

import it.polimi.middleware.spark.utils.MiscUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.from_json;

public class SparkProcessor {

    public static void main(String[] args) throws TimeoutException, StreamingQueryException {

        MiscUtils.setLogLevel();

        // Use default values if not specified otherwise
        // Default values work in a local environment in IntelliJ
        final String master = args.length > 0 ? args[0] : "local[*]";
        final String bootstrap = args.length > 1 ? args[1] : "localhost:9092";
        final String topic = args.length > 2 ? args[2] : "rawNoise";
        final String filePath = args.length > 3 ? args[3] : "src/main/resources/";
        final String fileName = args.length > 4 ? args[4] : "poi_map.jsonl";

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
                .json(filePath + fileName);

        //TODO Remove in final release
        poiDataset.show();

        /* END-SECTION */
        /* SECTION: Streaming table */

        // Define the schema for each string coming from external applications
        StructType rawSchema = DataTypes.createStructType(new StructField[] {
                DataTypes.createStructField("x", DataTypes.DoubleType, true),
                DataTypes.createStructField("y", DataTypes.DoubleType, true),
                DataTypes.createStructField("val", DataTypes.createArrayType(DataTypes.DoubleType), true),
        });

        // Create a row for each Kafka event
        // "value" field of each row is parsed as a string ("value" is the only field of interest)
        Dataset<Row> rawRecord = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", bootstrap)
                .option("subscribe", topic)
                .option("startingOffsets", "latest") // "earliest"
                .load()
                .selectExpr("CAST(value AS STRING)");

        // Clean the rows received from Kafka and apply the schema to the dataset
        Dataset<Row> rawDataset = rawRecord
                .select(from_json(col("value"), rawSchema).as("data"))
                .select("data.*");

        /* END-SECTION */
        /* SECTION: Data cleaning and enrichment */

        //TODO Clean the dataset from invalid measurements
        Dataset<Row> cleanDataset = rawDataset.na().drop();

        //TODO Transform the dataset associating a POI to each measurement
        Dataset<Row> richDataset = cleanDataset;

        /* END-SECTION */
        /* SECTION: Data analysis */

        //TODO Enhance data analysis

        /* END-SECTION */
        /* SECTION: Store results */

        //TODO Store results in a Kafka topic

        //TODO Remove in final release
        cleanDataset
                .writeStream()
                .format("console")
                .outputMode("append")
                .start()
                .awaitTermination();

        /* END-SECTION */

        spark.close();
    }
}
