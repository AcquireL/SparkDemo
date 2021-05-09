package com.startarget.scala.cdp.test

import java.util.ResourceBundle

import com.startarget.scala.cdp.util.Constants
import com.startarget.scala.cdp.connector.hdfs.read.HdfsSourceBuild
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object HdfsSourceTest {
  def main(args: Array[String]): Unit = {
    val sparkConf= new SparkConf().setMaster("local[2]").setAppName("HdfsSourceTest")
    val sparkContext= new SparkContext(sparkConf)
    val param= ResourceBundle.getBundle("application")
    // val basePath=param.getString(Constants.HDFSSOURCE_PATH)

    val basePath="hdfs://learn:9000/data/output/parquet/2021-04-13--23"
    val value: RDD[String] = HdfsSourceBuild.getReadHdfsFile(sparkContext, basePath)
    value.foreach(println)

    sparkContext.stop()
  }
}
