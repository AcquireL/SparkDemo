package com.startarget.sparkdemo.streaming.connector.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


public class WriteHdfs {
    public static void main(String[] args) throws StreamingQueryException {

        //构建SparkSession
        SparkConf conf= new SparkConf().setMaster("local[2]").setAppName("savehdfstest");
        // JavaStreamingContext sc = new JavaStreamingContext(conf, new Duration(6 * 1000));
        SparkSession spark= SparkSession.builder()
                .config(conf)
                .getOrCreate();

        /**
         * 因为只是测试，所以就从端口中读数据
         *
         * 在本地的虚拟机上执行 nc -lt 9999 来往这个端口上发数据
         * 数据格式：
         * 用户主键     时间戳     访问的url
         * user01,1584433934161,www.qq.com
         * user01,1584433934261,www.qq.com
         *
         */

        Dataset<Row> lines  = spark.readStream()
                .format("socket")
                .option("host", "localhost")
                .option("port", 9999)
                .load();

        //将数据加载成对象
        Dataset<RequestDomain> requestDomains = lines
                .as(Encoders.STRING())
                .flatMap((FlatMapFunction<String, RequestDomain>) x -> {
                    String[] arr = x.split(",");
                    return Arrays.asList(new RequestDomain(arr[0],arr[2],arr[1])).iterator();
                }, Encoders.bean(RequestDomain.class));

        //将数据repartition成5个区，写入数据到hdfs
        StreamingQuery query = requestDomains.repartition(5).writeStream()
                .outputMode("append")
                .format("csv")
                // .option("checkpointLocation", "hdfs://learn:9000/HDFSForeach/check")
                .foreach(new HDFSForeachWriter<RequestDomain>())
                //.option("path", "D://path/data")
                .start();

        query.awaitTermination();

    }

    static class HDFSForeachWriter<RequestDomain> extends ForeachWriter<com.startarget.sparkdemo.streaming.connector.hdfs.RequestDomain > {

        FileSystem fs = null;
        Long partitionId = null;
        String schema = "hdfs://learn:9000";
        @Override
        public boolean open(long partitionId, long version) {
            Configuration conf = new Configuration();
            this.partitionId = partitionId;

            /**
             * 注意，为了让hdfs支持append功能，需要有以下设置(hdfs集群上也得添加这些设置，并且重启服务)！否则会报错
             * 具体配置的详情，大家可以百度，或者给我留言
             */
            conf.setBoolean("dfs.support.append", true);
            conf.set("fs.defaultFS",schema);
            conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
            conf.setBoolean("fs.hdfs.impl.disable.cache", true);
            try {
                fs = FileSystem.get(conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }


        @Override
        public void process(com.startarget.sparkdemo.streaming.connector.hdfs.RequestDomain  value) {

            String writeFileName = getWriteFileName(value);

            ByteArrayInputStream in = new ByteArrayInputStream(value.toString().getBytes());
            OutputStream out = null;
            try {
                Path path = new Path(writeFileName);
                //文件存在就append，不存在就create
                if(!fs.exists(path)){
                    out = fs.create(path);
                }else {
                    out = fs.append(path);
                }
                //该方法会帮你写入数据，并且关闭in和out流
                IOUtils.copyBytes(in, out, 4096, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close(Throwable errorOrNull) {
            try {
                fs.close();
                fs = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 获得该条数据需要写到哪个文件块中
         * @param value
         * @return
         */
        private String getWriteFileName(com.startarget.sparkdemo.streaming.connector.hdfs.RequestDomain value) {
            try {
                Long ts =Long.valueOf(value.getTs());
                SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd-HH-mm");
                String minute = sdf.format(new Date(Long.parseLong(String.valueOf(ts))));
                String date = minute.substring(0, 8);
                //将10分钟内的数据都归入到统一目录下
                minute = minute.substring(0, minute.length() - 1) + "0";
                String FileNameMatch = schema + "/" + date + "/" + minute + "-" + partitionId + "-";
                Path path = new Path(FileNameMatch + "*");

                //通过globStatus来匹配该时间段该分区的所有文件
                FileStatus[] fileStatuses =  fs.globStatus(path);
                //要判断文件大小来写入数据
                if (fileStatuses.length != 0) {
                    FileStatus finalFileName = fileStatuses[fileStatuses.length - 1];
                    /**
                     * 单位是byte，1M = 1048576 byte
                     * 这里为了测试就写了1M，一般我们需要hdfs文件块大小在200M左右
                     */
                    if (finalFileName.getLen() < 1048576) {
                        return FileNameMatch + String.valueOf(fileStatuses.length - 1) + ".txt";
                    } else {
                        return FileNameMatch + String.valueOf(fileStatuses.length) + ".txt";
                    }
                } else {
                    return FileNameMatch + "0" + ".txt";
                }

            } catch (IOException e) {
                e.printStackTrace();
                //如果出了异常，可以返回一个延迟的数据目录，后续集中处理这些异常数据
                return schema + "/delay/data.txt";
            }
        }
    }
}
