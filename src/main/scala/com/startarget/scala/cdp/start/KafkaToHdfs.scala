package com.startarget.scala.cdp.start

import java.io.BufferedOutputStream
import java.net.URI
import java.text.SimpleDateFormat
import java.util.{Date, ResourceBundle}

import com.startarget.scala.cdp.util.Constants
import com.startarget.scala.cdp.connector.kafka.customer.KafkaSourceBuild
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.compress.{CompressionCodec, CompressionOutputStream}
import org.apache.hadoop.util.ReflectionUtils
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

import scala.collection.immutable.HashMap


/**
 *  SparkStreaming标准化落hdfs
 */
object KafkaToHdfs {

  def main(args: Array[String]): Unit = {
    val logger=LoggerFactory.getLogger(KafkaToHdfs.getClass)

    val params: ResourceBundle = ResourceBundle.getBundle("application")
    val basePath=params.getString(Constants.HDFSSINK_PATH)

    val sparkConf= new SparkConf().setAppName("kafkaToHdfs").setMaster("local[2]")
    val sc = new SparkContext(sparkConf)
    val ssc=new StreamingContext(sparkConf,Seconds(2))

    val kafkaStream: InputDStream[ConsumerRecord[String, String]] = KafkaSourceBuild.getKafkaSource(params,ssc)

    kafkaStream.foreachRDD(rdd => {
      val erroracountor = rdd.sparkContext.collectionAccumulator[Exception]
      rdd.foreachPartition(p => {
        var writerMap: Map[String, (String, CompressionOutputStream)] = new HashMap()
        val conf = new Configuration()
        val fs = FileSystem.get(new URI("hdfs://learn:9000"), conf, "lwj")
        // val fs = FileSystem.get(conf)
        var flushcount = 0
        try {
          while (p.hasNext) {
            flushcount += 1

            val record_current = p.next()
            val topic = record_current.topic()
            val partition = record_current.partition()
            val timstm_kfk = record_current.timestamp()
            val kfk_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(timstm_kfk)
            val kafka_date = new SimpleDateFormat("yyyyMMdd").format(timstm_kfk)
            val offset = record_current.offset().toString()
            val kfkkey = record_current.key()
            val message = record_current.value()
            val timstm_sys = new Date().getTime

            // 按行写入hdfs
            val line = partition + "|" + offset + "|" + kfkkey + "|" + kfk_time + "|" + message
            var msg: Array[Byte] = (line + "\n").getBytes("UTF-8")

            // 拼接写入hdfs的路径
            var partition_day = new SimpleDateFormat("yyyyMMdd").format(timstm_kfk)
            partition_day = "/dt=" + partition_day + "/"
            var savingpath: String = null
            if (basePath.endsWith("/")) {
              savingpath = basePath + topic + partition_day
            } else {
              savingpath = basePath + "/" + topic + partition_day
            }

            // 写入hdfs的核心逻辑
            if (writerMap.contains(savingpath)) {
              writerMap.apply(savingpath)._2.write(msg)
              //flush data out of memory
              if (flushcount >= 50000) {
                val writers = writerMap.keys.toArray
                writers.foreach(w => {
                  val writer_current = writerMap.apply(w)
                  writer_current._2.flush()
                })
                flushcount = 0
                logger.info("!!!>>>Flushing data out of memory.")
              }
            } else {
              //向map里添加writer
              val tmpname = "SIGNALDATA_" + kafka_date + "_" + timstm_sys.toString() + ".snappy.tmp"
              val filename = "SIGNALDATA_" + kafka_date + "_" + timstm_sys.toString() + ".snappy"
              val conf = new Configuration()
              val path = new Path(savingpath + tmpname)
              var writer: CompressionOutputStream = null
              val output = fs.create(path)
              val codec = ReflectionUtils.newInstance(Class.forName("org.apache.hadoop.io.compress.SnappyCodec"), conf).asInstanceOf[CompressionCodec]
              writer = codec.createOutputStream(new BufferedOutputStream(output))
              writerMap += (savingpath -> (savingpath + filename, writer))
              //向writer中写入数据
              writer.write(msg)
            }
          }
        } catch {
          case e: Exception => {
            logger.error("Found Error:" + e)
            erroracountor.add(e)
          }
        } finally {
          val writers = writerMap.keys.toArray
          writers.foreach(w => {
            val writer_current = writerMap.apply(w)
            writer_current._2.close()
            val tmppath = new Path(writer_current._1 + ".tmp")
            val evnpath = new Path(writer_current._1)
            fs.rename(tmppath, evnpath)
            logger.info("!!!>>>renaming file:" + writer_current._1)
          })
        }
      })

      // 打印异常
      val totalerr = erroracountor.value
      if (totalerr.size() != 0) {
        val errorsize = totalerr.size()
        for (a <- 0 until errorsize) {
          logger.error("###>>>Found Exception<<<###")
          logger.error("Exception: " + totalerr.get(a))
        }
        System.exit(0)
      }

      kafkaStream.asInstanceOf[CanCommitOffsets].commitAsync(rdd.asInstanceOf[HasOffsetRanges].offsetRanges)

    })

    ssc.start()
    ssc.awaitTermination()

  }

}
