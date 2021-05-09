package com.startarget.scala.sparkDemo.sparkCore.model

import scala.collection.immutable.Map
/*
    定义最终输出类型（消息ID(mid),设备ID(did),应用ID (aid),数据模块ID (sid),数据模块版本ID(svid),
    数据采集时间戳(ts),采集数据(d))
 */
case class DataEntity(mid:String,did:Double,aid:String,sid:String,svid:String,ts:String,data:Map[String,String])

