package com.startarget.scala.sparkDemo.sparkSql

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object SinkToKafka {

  def main(args: Array[String]): Unit = {
    val sparkConfig =new SparkConf().setAppName("SinkToKafka").setMaster("local[2]")
    val spark = SparkSession.builder().config(sparkConfig).getOrCreate()
    val sourceDF: DataFrame = spark.createDataFrame(Array(("a",null),("b",null))).toDF("col1","col2")
    val jsonDataSet = sourceDF.toJSON

    jsonDataSet.write
      .format("kafka")
      .option("kafka.bootstrap.servers", "AcquireL:9092")
      .option("topic", "test")
      .save()

    spark.stop()
  }

}
