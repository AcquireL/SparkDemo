package com.startarget.scala.cdp.connector.hdfs.read

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD


class HdfsSourceBuild{
  // 读取hdfs上的文件
  def readHdfsFile(sc:SparkContext,path:String): RDD[String] ={
    sc.textFile(path)
  }
}


object HdfsSourceBuild {
  def getReadHdfsFile(sc:SparkContext,path:String): RDD[String] ={
    val hdfsSourceBuild= new HdfsSourceBuild()
    hdfsSourceBuild.readHdfsFile(sc,path)
  }
}
