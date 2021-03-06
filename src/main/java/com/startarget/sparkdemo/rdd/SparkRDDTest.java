package com.startarget.sparkdemo.rdd;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

public class SparkRDDTest {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("SparkRDDTest").setMaster("local[*]");
        JavaSparkContext jsc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        JavaRDD<Integer> distData = jsc.parallelize(data);
        distData.foreach(k->{
            System.out.println(k);
        });
        jsc.stop();
    }
}
