package com.startarget.scala.cdp.test

import java.util.ResourceBundle

import com.startarget.scala.cdp.connector.kafka.customer.KafkaSourceBuild
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}

/**
 *  测试消费kafka中的数据
 */
object KafkaSourceTest {
  def main(args: Array[String]): Unit = {

    val sparkConf= new SparkConf().setAppName("kafkaToHdfsTest").setMaster("local[2]")
    val ssc=new StreamingContext(sparkConf,Seconds(2))
    val param= ResourceBundle.getBundle("application")

    val kafkaStream: InputDStream[ConsumerRecord[String, String]] =KafkaSourceBuild.getKafkaSource(param,ssc)

    val inputDStream: DStream[String] = kafkaStream.map(consumerRecord=>consumerRecord.value())

    inputDStream.print()

    ssc.start()

    ssc.awaitTermination()

  }
}
