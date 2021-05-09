package com.startarget.scala.sparkDemo.sparkStreaming.connector.kafka

import java.util.regex.Pattern

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.{InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

/**
 * sparkstreaming消费kafka中的数据（使用direct方式）
 */
object SparkConsumerKafkaByDirect {
  def main(args: Array[String]): Unit = {
    // val sc = getSparkContext("SparkConsumerKafkaByDirect", "local[2]")
    val sc= SparkContext.getOrCreate(new SparkConf())
    val ssc = new StreamingContext(sc, Seconds(6))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "learn:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "spark_vdf_multi",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    //val topics = Array("VDF_TEST_1", "VDF_TEST_2")

    val pattern = Pattern.compile("VDF[A-Za-z0-9_]*")

    val stream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.SubscribePattern[String, String](pattern,kafkaParams)
    )

    val data = stream.map(record => (record.key, record.value))

    data.print()
    ssc.start()
    ssc.awaitTermination()
  }
}
