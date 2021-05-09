package com.startarget.scala.sparkDemo.sparkStreaming.connector

import org.apache.spark.{SparkConf, SparkContext}

package object kafka {
  def getSparkContext(name:String,master:String="local[*]"): SparkContext ={
    SparkContext.getOrCreate(new SparkConf().setAppName(name).setMaster(master))
  }
}
