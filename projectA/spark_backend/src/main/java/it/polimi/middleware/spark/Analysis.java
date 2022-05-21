package it.polimi.middleware.spark;

import it.polimi.middleware.spark.utils.MiscUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.*;

public class Analysis {

    public static void main(String[] args) {

        MiscUtils.setLogLevel();

        // Use default values if not specified otherwise
        // Default values work in a local environment in IntelliJ
        final String master = args.length > 0 ? args[0] : "local[*]";
        final String noiseDataLocation = args.length > 1 ? args[1] : "/tmp/cleaning_enrichment/noise_data";
        final String checkpointLocation = args.length > 2 ? args[2] : "/tmp/analysis/checkpoint";

        System.out.println("[LOG] Spark started with the following parameters:\n" +
                "[LOG] spark.master: " + master + "\n" +
                "[LOG] noise.data.location: " + noiseDataLocation + "\n" +
                "[LOG] checkpoint.location: " + checkpointLocation);

        SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("Analysis")
                .getOrCreate();

        final Dataset<Row> fullDataset = spark
                .read()
                .format("csv")
                .option("header", "true")
                .option("delimiter", ",")
                .csv(noiseDataLocation + "/*/*.csv");

        System.out.println("[DEBUG] Fetched dataset with " + fullDataset.count() + "elements");

        Dataset<Row> filteredDemo = fullDataset
                .withColumn("current", current_timestamp().minus(expr("INTERVAL 1 HOUR")))
                .where(col("ts").gt(col("current")));

        filteredDemo.show(20, false);

        spark.close();
    }
}
