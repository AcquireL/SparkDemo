package com.startarget.sparkdemo.sparkCore.test;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class WordCountTest {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[4]").setAppName("wordCountTest");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);

        JavaRDD<String> line = jsc.textFile("./README.md",1);

        line.foreach(rdd->{
            System.out.println(rdd);
        });

        System.out.println(jsc.getConf());
    }
}
