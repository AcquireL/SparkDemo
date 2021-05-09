package com.startarget.scala.cdp.test

import java.util.ResourceBundle

import com.google.gson.Gson
import com.startarget.scala.cdp.connector.hbase.writeTable.HbaseSinkTableBuild
import com.startarget.scala.cdp.connector.kafka.customer.KafkaSourceBuild
import com.startarget.scala.cdp.model.User
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}

object KafkaToHbaseTest {
  def main(args: Array[String]): Unit = {
    val sparkConf= new SparkConf().setAppName("kafkaToHbase").setMaster("local[2]")
    val ssc=new StreamingContext(sparkConf,Seconds(2))
    val param= ResourceBundle.getBundle("application")

    val kafkaStream: InputDStream[ConsumerRecord[String, String]] =KafkaSourceBuild.getKafkaSource(param,ssc)

    val inputDStream: DStream[Object] = kafkaStream.map(consumerRecord=>{
      val json= consumerRecord.value()
      new Gson().fromJson(json,classOf[User])
    })

    inputDStream.print()

    HbaseSinkTableBuild.getHbaseSink(ssc.sparkContext,inputDStream)

    ssc.start()

    ssc.awaitTermination()
  }
}
