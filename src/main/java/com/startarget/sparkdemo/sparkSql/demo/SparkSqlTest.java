package com.startarget.sparkdemo.sparkSql.demo;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

import static org.apache.spark.sql.functions.count;


public class SparkSqlTest {
    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession.builder().appName("test").master("local").getOrCreate();
        Dataset<String> stringDataset = sparkSession.read().textFile("./README.md");
        stringDataset.show();
        Dataset<Row> select = stringDataset
                .withColumn("key", functions.lit(1))
                .select("key", "value");
        select.show();

        Dataset<Row> agg = select.groupBy("key").agg(count("value"));
        agg.show();


        sparkSession.close();
    }
}
