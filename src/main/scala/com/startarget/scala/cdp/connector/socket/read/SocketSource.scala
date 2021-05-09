package com.startarget.scala.cdp.connector.socket.read

import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

/**
 * 接受Socket发送的数据
 */
object SocketSource {

  // 获取Socket的数据 ( socket中的数据以逗号分隔 例如：name，kali)
  def getSource(ssc:StreamingContext): DStream[Tuple2[String,String]] = {
    val socketStream= ssc.socketTextStream("localhost",9999)

    val stream= socketStream.map(line=>{
      val world: Array[String] =line.split(",")
      new Tuple2[String,String](world.apply(0),world.apply(1))
    })
    stream
  }
}
