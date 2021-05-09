package com.startarget.scala.cdp.connector.kafka.producer

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.streaming.dstream.DStream

/**
 * 构建kafka生产者
 */

object KafkaSinkBuild {

  // 初始化properties
  def initKafkaConf(): Properties = {
    val p = new Properties()
    p.setProperty("bootstrap.servers", "learn:9092")
    p.setProperty("key.serializer", classOf[StringSerializer].getName)
    p.setProperty("value.serializer", classOf[StringSerializer].getName)
    p
  }

  /**
   * 将数据写入kafka中
   * @param dataStream
   */
  def sinkToKafka(dataStream: DStream[String]) {
    val props = initKafkaConf()

    dataStream.foreachRDD(rdd => {
      rdd.foreach(line => {
        // producer需要在每个task中生成，不能定义到foreach外面
        val producer = new KafkaProducer[String, String](props)
        val message = new ProducerRecord[String, String]("topic1", null, line)
        producer.send(message)
        producer.close()
      })
    })

  }
}
