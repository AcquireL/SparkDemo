package com.startarget.scala.cdp.test

import com.startarget.scala.cdp.connector.kafka.producer.KafkaSinkBuild
import com.startarget.scala.cdp.connector.socket.read.SocketSource
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 *  测试将数据写入kafka
 */
object SocketToKafkaTest {
  def main(args: Array[String]): Unit = {

    val sparkConf= new SparkConf().setAppName("kafkaToHdfsTest").setMaster("local[2]")

    val ssc=new StreamingContext(sparkConf,Seconds(2))

    val inputDs: DStream[(String, String)] = SocketSource.getSource(ssc)

    val sinkDs= inputDs.map(tuple=>tuple._1+tuple._2)

    KafkaSinkBuild.sinkToKafka(sinkDs)

    ssc.start()

    ssc.awaitTermination()

  }
}
