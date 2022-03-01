package com.startarget.sparkdemo.sparkSql.demo;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

public class InitSparkSession {
    public static void main(String[] args) {

        // 1、直接初始化
        SparkSession spark = SparkSession
                .builder()
                .appName("Java Spark SQL basic example")
                .master("local[2]")
                .getOrCreate();

        System.out.println(spark.catalog());
        spark.close();
    }
}
