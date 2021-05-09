package com.startarget.scala.cdp.connector.hdfs.write

import java.io.BufferedOutputStream
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{IOUtils, NullWritable, SequenceFile, Text}
import org.apache.hadoop.io.SequenceFile.{CompressionType, Writer}
import org.apache.hadoop.io.compress.{CompressionCodec, CompressionOutputStream}
import org.apache.hadoop.util.ReflectionUtils
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkContext
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.immutable.HashMap


class HdfsSinkBuild() {
  private val logger = LoggerFactory.getLogger(HdfsSinkBuild.getClass)

  // kafka数据标准化落hdfs (有问题，需要查看foreach和foreachPatition的区别，foreachPartition会存在序列化问题）
  def fromKafkaSinkHdfs(kafkaStream: InputDStream[ConsumerRecord[String, String]], basePath: String): Unit = {

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
  }


  /**
   * 根据输入流和path直接标准化落地到hdfs上(使用Writer方法）
   *
   * @param inputDS
   */
  def sinkHdfs(inputDS: DStream[String], path: String): Unit = {
    inputDS.foreachRDD(rdd => {
      //向writer中写入数据
      rdd.foreach(line => {
        val conf = new Configuration()
        val writerKey = NullWritable.get()
        val writer: Writer = SequenceFile.createWriter(conf, Writer.file(new Path(path + "SIGNALDATA.txt")),
          Writer.keyClass(classOf[NullWritable]), Writer.valueClass(classOf[Text]),
          Writer.compression(CompressionType.NONE))
        val text = new Text(line)
        writer.append(writerKey, text)
        IOUtils.closeStream(writer)
      })
    })
  }

}


object HdfsSinkBuild {
  // kafka数据标准化落hdfs
  def getFromKafkaSinkHdfs(kafkaStream: InputDStream[ConsumerRecord[String, String]], basePath: String) ={
    val hdfsSinkBuild= new HdfsSinkBuild()
    // hdfsSinkBuild.fromKafkaSinkHdfs(kafkaStream: InputDStream[ConsumerRecord[String, String]], basePath: String)
  }

  // 将普通数据流写入hdfs中
  def getSinkHdfs(inputDS: DStream[String], path: String): Unit ={
    val hdfsSinkBuild= new HdfsSinkBuild()
    hdfsSinkBuild.sinkHdfs(inputDS,path)
  }


}
