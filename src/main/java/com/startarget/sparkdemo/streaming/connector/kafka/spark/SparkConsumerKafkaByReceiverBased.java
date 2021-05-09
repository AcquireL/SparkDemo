package com.startarget.sparkdemo.streaming.connector.kafka.spark;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.streaming.kafka010.OffsetRange;
import scala.Tuple2;

import java.util.HashMap;
import java.util.Map;

public class SparkConsumerKafkaByReceiverBased {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName("SparkConsumerKafkaByReceiverBased")
                .setMaster("local[*]")
                .set("spark.streaming.receiver.writeAheadLog.enable","true"); //开启WAL预写日志，保存数据源的可靠性;
        JavaSparkContext jsc = new JavaSparkContext(conf);
        // JavaStreamingContext ssc = new JavaStreamingContext(jsc, new Duration(6 * 1000));
        // ssc.checkpoint("./Kafka_Receiver");

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", "learn:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "spark_vdf_multi");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        OffsetRange offsetRange=OffsetRange.create(new TopicPartition("VDF_TEST_1",0),176800,176900);
        OffsetRange[] offsetRanges=new OffsetRange[10];
        offsetRanges[0]=offsetRange;

        // 这里的value并不是topic分区数，它表示的topic中每一个分区被N个线程消费
        Map<String,Integer> topic=new HashMap<>();

        JavaRDD<ConsumerRecord<String, String>> rdd =
                KafkaUtils.createRDD(
                        jsc,
                        kafkaParams,
                        offsetRanges,
                        LocationStrategies.PreferConsistent()
                );

        rdd.foreach(k->{
            System.out.println(k);
        });


        JavaRDD<Tuple2<String, String>> map = rdd.map(s -> new Tuple2<String, String>(s.key(), s.value()));

        map.foreach(k->{
            System.out.println(k._1);
            System.out.println(k._2);
        });

        jsc.stop();
        jsc.close();

    }
}
