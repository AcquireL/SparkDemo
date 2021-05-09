package com.startarget.scala.cdp.test

import com.startarget.scala.cdp.connector.hdfs.write.HdfsSinkBuild
import com.startarget.scala.cdp.connector.socket.read.SocketSource
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.DStream

object SocketToHdfsTest {
  def main(args: Array[String]): Unit = {

    val sparkConf= new SparkConf().setAppName("socketToHdfsTest").setMaster("local[2]")
    val ssc=new StreamingContext(sparkConf,Seconds(5))

    val socketStream: DStream[(String, String)] = SocketSource.getSource(ssc)

    val inputDStream: DStream[String] = socketStream.map(tuple2=>tuple2._1+tuple2._2)

    inputDStream.print()

    val path="hdfs://learn:9000/data/spark/cdp/socket"

    HdfsSinkBuild.getSinkHdfs(inputDStream,path)

    ssc.start()
    ssc.awaitTermination();

  }

}
