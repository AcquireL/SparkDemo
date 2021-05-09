package com.startarget.scala.cdp.connector.kafka.customer


import java.util.ResourceBundle

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.{KafkaUtils, LocationStrategies}
import com.startarget.scala.cdp.util.Constants

/**
 * 构建kafka消费者
 */
class KafkaSourceBuild(params:ResourceBundle){
  // 初始化properties
  def initKafkaConf(): Map[String,Object] = {
    var propertiesMap= Map[String, Object](
      "bootstrap.servers" -> params.getString(Constants.KAFKASOURCE_BOOTSTRAP_SERVERS),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> params.getString(Constants.KAFKASOURCE_GROUPID),
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> params.getString(Constants.KAFKASOURCE_AUTOCOMMIT)
    )
    // 判断是否需要过kerberos
    val isKerberos=params.getString(Constants.KAFKASOURCE_KERBEROS).toBoolean
    if (isKerberos) {
      propertiesMap += ("sasl.kerberos.service.name" -> "kafka")
      propertiesMap += ("security.protocol" -> "SASL_PLAINTEXT")
    }
    propertiesMap
  }

  //  使用Direct获取kafka中的数据
  def getDataStreamFromKaka(ssc:StreamingContext): InputDStream[ConsumerRecord[String, String]] ={
    val props: Map[String, Object] = initKafkaConf()
    val topics= params.getString(Constants.KAFKASOURCE_TOPICS).split(",")
    val stream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      Subscribe[String, String](topics, props)
    )
    stream
  }

}


object KafkaSourceBuild {
    // 获取kafkaSource
  def getKafkaSource(params:ResourceBundle,ssc:StreamingContext)={
    val kafkaSourceBuild= new KafkaSourceBuild(params)
    kafkaSourceBuild.getDataStreamFromKaka(ssc)
  }


}
