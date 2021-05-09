package com.startarget.scala.cdp.connector.hbase.writeTable

import com.startarget.scala.cdp.model.User
import org.apache.hadoop.hbase.client.{Put, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.spark.SparkContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.rdd.RDD

class HbaseSinkTableBuild{
  // 写入hbase测试
  def writeHbase(sc:SparkContext,inputData:DStream[Object]): Unit = {

    val tablename = "learn:test"
    sc.hadoopConfiguration.set("hbase.zookeeper.quorum", "learn")
    sc.hadoopConfiguration.set("hbase.zookeeper.property.clientPort", "2181")
    sc.hadoopConfiguration.set(TableOutputFormat.OUTPUT_TABLE, tablename)

    val job = new Job(sc.hadoopConfiguration)
    job.setOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setOutputValueClass(classOf[Result])
    job.setOutputFormatClass(classOf[TableOutputFormat[ImmutableBytesWritable]])

    inputData.foreachRDD(rdd => {
      val result: RDD[(ImmutableBytesWritable, Put)] = rdd.map(line => {
        val user=line.asInstanceOf[User]
        val put = new Put(Bytes.toBytes(user.id))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes(user.name))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("age"), Bytes.toBytes(user.age))
        (new ImmutableBytesWritable, put)
      })
      result.saveAsNewAPIHadoopDataset(job.getConfiguration())
    })

  }
}



object HbaseSinkTableBuild{
  def getHbaseSink(sc:SparkContext,inputData:DStream[Object]){
    val hbaseSinkTableBuild= new HbaseSinkTableBuild()
    hbaseSinkTableBuild.writeHbase(sc,inputData)
}
}
