package com.startarget.scala.sparkDemo.sparkCore.model

/**
 *  Mysql中获取出来的数据实体
 */
class MysqlDataEntity {
  private var topic:String = _
  private var schema:String = _
  private var targetHdfsUrl:String= _

  // 定义构造函数
  def this(topic:String,schema:String,targetHdfsUrl:String){
    // 最终都需要调用默认this()默认构造
    this();
    this.topic =topic;
    this.schema=schema;
    this.targetHdfsUrl=targetHdfsUrl;
  }

  // 定义get和set方法
  def getTopic=topic
  def setTopic(initTopic:String){
    topic=initTopic
  }

  def getSchema=schema
  def setSchema(initSchema:String): Unit ={
    schema=initSchema
  }

  def getTargetHdfsUrl=targetHdfsUrl
  def setTargetHdfsUrl(initTargetHdfsUrl:String): Unit ={
    targetHdfsUrl=initTargetHdfsUrl
  }


  override def toString = s"MysqlDataEntity($topic, $schema, $targetHdfsUrl)"
}
