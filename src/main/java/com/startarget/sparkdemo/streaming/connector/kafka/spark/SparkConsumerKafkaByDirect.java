package com.startarget.sparkdemo.streaming.connector.kafka.spark;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import scala.Tuple2;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *  sparkstreaming消费kafka中的数据（使用direct方式）
 */
public class SparkConsumerKafkaByDirect {

    public static void main(String[] args) throws Exception {

        SparkConf conf = new SparkConf().setAppName("SparkConsumerKafkaByDirect").setMaster("local[*]");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, new Duration(6 * 1000));

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", "learn:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "spark_vdf_multi");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Pattern pattern = Pattern.compile("VDF[A-Za-z0-9_]*");

        // Collection<String> topics = Arrays.asList("VDF_TEST_1", "VDF_TEST_2");

        final JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        ssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>SubscribePattern(pattern, kafkaParams)
                );


        JavaPairDStream<String, String> keyValueDStream = stream.mapToPair(
                new PairFunction<ConsumerRecord<String, String>, String, String>() {
                    @Override
                    public Tuple2<String, String> call(ConsumerRecord<String, String> record) {
                        return new Tuple2<>(record.key(), record.value());
                    }
                });

        keyValueDStream.print();
        ssc.start();
        ssc.awaitTermination();
        ssc.close();
    }
}
