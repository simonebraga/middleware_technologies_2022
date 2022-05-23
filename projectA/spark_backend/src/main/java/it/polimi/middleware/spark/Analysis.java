package it.polimi.middleware.spark;

import it.polimi.middleware.spark.utils.MiscUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.Scanner;

import static org.apache.spark.sql.functions.*;

public class Analysis {

    private static void printInstructions() {
        System.out.println("Type \"quit\" to stop the analysis module or perform one of the following queries:\n" +
                "\"q1h\" | \"q1d\" | \"q1w\" - Hourly, daily, and weekly moving average of noise level, for each point of interest.\n" +
                "\"q2\" - Top 10 points of interest with the highest level of noise over the last hour.\n" +
                "\"q3\" - Point of interest with the longest streak of good noise level.");
    }

    public static void main(String[] args) {

        MiscUtils.setLogLevel();

        // Use default values if not specified otherwise
        // Default values work in a local environment in IntelliJ
        final String master = args.length > 0 ? args[0] : "local[*]";
        final String noiseDataLocation = args.length > 1 ? args[1] : "/tmp/cleaning_enrichment/noise_data";
        final String checkpointLocation = args.length > 2 ? args[2] : "/tmp/analysis/checkpoint";
        final int poi_amount = args.length > 3 ? Integer.parseInt(args[3]) : 10;

        System.out.println("[LOG] Spark started with the following parameters:\n" +
                "[LOG] spark.master: " + master + "\n" +
                "[LOG] noise.data.location: " + noiseDataLocation + "\n" +
                "[LOG] checkpoint.location: " + checkpointLocation);

        SparkSession spark = SparkSession
                .builder()
                .master(master)
                .config("spark.scheduler.mode", "FAIR") // Needed when running multi-thread queries
                .appName("Analysis")
                .getOrCreate();

        /* SECTION: Dataset initialization */

        // Define the schema for each line of the .csv source file
        StructType richSchema = DataTypes.createStructType(new StructField[] {
                DataTypes.createStructField("poi_id", DataTypes.StringType, true),
                DataTypes.createStructField("val", DataTypes.DoubleType, true),
                DataTypes.createStructField("timestamp", DataTypes.TimestampType, true),
        });

        // Initialize the static dataset with all the noise records
        Dataset<Row> fullDataset = spark
                .read()
                .schema(richSchema)
                .format("csv")
                .option("header", "true")
                .option("delimiter", ",")
                .csv(noiseDataLocation + "/*/*.csv"); // This means that every .csv file is used as a source

        /* END-SECTION */
        /* SECTION: Data analysis */

        // Q1: Hourly, daily, and weekly moving average of noise level, for each point of interest
        // Implementation: Select rows from the last hour/day/week, aggregate over "poi_id" performing the average of "val"

        Dataset<Row> hourlyAverage = fullDataset
                .where(col("timestamp").gt(current_timestamp().minus(expr("INTERVAL 1 HOUR"))))
                .select("poi_id", "val")
                .groupBy("poi_id")
                .avg("val")
                .withColumnRenamed("avg(val)", "avg_noise");

        Dataset<Row> dailyAverage = fullDataset
                .where(col("timestamp").gt(current_timestamp().minus(expr("INTERVAL 1 DAY"))))
                .select("poi_id", "val")
                .groupBy("poi_id")
                .avg("val")
                .withColumnRenamed("avg(val)", "avg_noise");

        Dataset<Row> weeklyAverage = fullDataset
                .where(col("timestamp").gt(current_timestamp().minus(expr("INTERVAL 1 WEEK"))))
                .select("poi_id", "val")
                .groupBy("poi_id")
                .avg("val")
                .withColumnRenamed("avg(val)", "avg_noise");

        // Q2: Top 10 points of interest with the highest level of noise over the last hour
        // Implementation: Starting from the hourly Q1, order by descending "avg_noise" and select first 10 rows

        Dataset<Row> top10poi = hourlyAverage
                .orderBy(desc("avg_noise"))
                .limit(10);

        // Q3: Point of interest with the longest streak of good noise level
        // Implementation: Select the last over-threshold value for each POI,
        // compute the time interval between the value and the current timestamp
        // then select the POI with the longest streak

        Double threshold = 70.0;
        Dataset<Row> noiseStreak = fullDataset
                .where(col("val").gt(threshold))
                .groupBy("poi_id")
                .agg(max("timestamp"))
                .withColumn("streak", current_timestamp().minus(col("max(timestamp)")))
                .orderBy(desc("streak"))
                .select("poi_id")
                .limit(1);

        /* END-SECTION */
        /* SECTION: Compute results */

        System.out.println("[LOG] Analysis module started.\n");
        printInstructions();
        Scanner in = new Scanner(System.in); String s;
        while (!(s=in.nextLine()).equals("quit")) {
            switch (s) {
                case "q1h":
                    System.out.println("\nOutput of query 1 (hourly):");
                    hourlyAverage.show(poi_amount, false);
                    break;
                case "q1d":
                    System.out.println("\nOutput of query 1 (daily):");
                    dailyAverage.show(poi_amount, false);
                    break;
                case "q1w":
                    System.out.println("\nOutput of query 1 (weekly):");
                    weeklyAverage.show(poi_amount, false);
                    break;
                case "q2":
                    System.out.println("\nOutput of query 2:");
                    top10poi.show(10, false);
                    break;
                case "q3":
                    System.out.println("\nOutput of query 3:");
                    noiseStreak.show(1, false);
                    break;
                default:
                    System.out.println("\nUnknown command.");
            }
            printInstructions();
        }

        /* END-SECTION */

        spark.close();
    }
}
