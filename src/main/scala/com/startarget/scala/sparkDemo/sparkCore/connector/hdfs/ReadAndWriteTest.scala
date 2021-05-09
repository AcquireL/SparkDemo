package com.startarget.scala.sparkDemo.sparkCore.connector.hdfs

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
 *   读取hdfs文件，解析json，将解析结果写入hdfs
 */

object ReadAndWriteTest {

  def main(args: Array[String]): Unit = {
    // 初始化执行环境
    val spark = SparkSession
      .builder
      .appName("ReadAndWriteTest")
      .master("local[2]")
      .getOrCreate()

    // 读取hdfs文件
    val path="hdfs://learn:9000/data/hive/spark_vdf/VDF_TEST_1/dt=20201217"
    val readFileRDD: RDD[String] = spark.sparkContext.textFile(path)

    readFileRDD.foreach(println)

    // 获取出rawData中的json数据
    val jsonRdd= readFileRDD
      .map(rawData=>{
        rawData.split('|')(4)
      })


    jsonRdd.foreach(println)

    spark.stop()

  }
}
