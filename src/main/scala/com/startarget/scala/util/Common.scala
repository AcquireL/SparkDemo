package com.startarget.scala.util

import java.text.SimpleDateFormat

object Common {




  /**
   *  根据基础路径、topic名称、kafkaTs
   * @param basePath
   * @param topic
   * @param kafkaTs
   * @return
   */
  def getPath(basePath: String, topic: String, kafkaTs: Long): String = {
    val kafkaDate=new SimpleDateFormat("yyyyMMdd").format(kafkaTs)
    val partitioinDay="/dt="+kafkaDate+"/"
    var eventualpath: String = null
    //path for now:/user/hive/warehouse_ext/DL/phev/GBT2/raw/signal/topic/dt=xxx/
    if (basePath.endsWith("/")) {
      eventualpath = basePath + topic + partitioinDay
    } else {
      eventualpath = basePath + "/" + topic + partitioinDay
    }
    eventualpath
  }






}
