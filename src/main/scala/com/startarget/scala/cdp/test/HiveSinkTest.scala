package com.startarget.scala.cdp.test

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object HiveSinkTest {
  def main(args: Array[String]): Unit = {
    val sparkConf= new SparkConf().setAppName("hiveSinkTest").setMaster("local[2]")
    val sparkSession= SparkSession
      .builder
      .config(sparkConf)
      // 指定hive的metastore的端口  默认为9083 在hive-site.xml中查看
      .config("hive.metastore.uris", "thrift://learn:9083")
      //指定hive的warehouse目录
      .config("spark.sql.warehouse.dir", "hdfs://learn:9000/user/hive/warehouse")
      //直接连接hive
      .enableHiveSupport()
      .getOrCreate()

    val sql = "select * from hive_1.user_info"
    sparkSession.sql(sql).show(10)

    sparkSession.stop()
  }
}
