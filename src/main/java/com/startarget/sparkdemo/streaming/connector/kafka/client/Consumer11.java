package com.startarget.sparkdemo.streaming.connector.kafka.client;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Consumer11 {
    //初始化properties参数
    private Properties initProperties(){
        Properties props = new Properties();
        String broker_list="learn:9092";
        props.put("bootstrap.servers", broker_list);
        props.put("group.id", "lwj1");//消费者组，只要group.id相同，就属于同一个消费者组
        props.put("enable.auto.commit", "true");//自动提交offset
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return  props;
    }

    //手动提交offset
    public void consumer11Test(){
        Properties props=initProperties();
        props.put("enable.auto.commit", "false");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("bs_test1","bs_test2","bs_test3"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
            consumer.commitSync();
//            consumer.close();
        }
    }
    //动态监测topic的生成
    public void consumerDynamicSubscribingTopic(){
        Properties props=initProperties();
        final ConcurrentLinkedQueue<String> subscribedTopics = new ConcurrentLinkedQueue<>();

        // 创建另一个测试线程，启动后首先暂停10秒然后变更topic订阅
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // swallow it.
                    }
                    List<String> topics = getKafkaTopic();
                    List<String> validTopics=new ArrayList<>();
                    Pattern pattern=Pattern.compile("bs_test[0-9]");
                    // 变更为订阅topic： bs_test2， bs_test3
                    for(String topic:topics){
                        Matcher matcher = pattern.matcher(topic);
                        if(matcher.find()){
                            validTopics.add(topic);
                        }
                    }
                    subscribedTopics.addAll(validTopics);
                }
            }
        };
        new Thread(runnable).start();

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // 最开始的订阅列表：bs_test1、bs_test2
        consumer.subscribe(Arrays.asList("bs_test1", "bs_test2"));
        while (true) {
            ConsumerRecords<String, String> records= consumer.poll(2000); //表示每2秒consumer就有机会去轮询一下订阅状态是否需要变更
            // 本例不关注消息消费，因此每次只是打印订阅结果！
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
            System.out.println(consumer.subscription());
            if (!subscribedTopics.isEmpty()) {
                Iterator<String> iter = subscribedTopics.iterator();
                List<String> topics = new ArrayList<>();
                while (iter.hasNext()) {
                    topics.add(iter.next());
                }
                subscribedTopics.clear();
                consumer.subscribe(topics);  // 重新订阅topic
            }
        }
        // 本例只是测试之用，使用了while(true)，所以这里没有显式关闭consumer
//        consumer.close();
    }

    //获取kafka中的所有topic
    private List<String>  getKafkaTopic() {
        Properties props=initProperties();
        List<String> topics = new ArrayList<>();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        Set<String> topicsSet = consumer.listTopics().keySet();
        for(String topic:topicsSet){
            topics.add(topic);
        }
        return topics;
    }



    public static void main(String[] args) {
        Consumer11 consumer = new Consumer11();
//        consumer.consumer11Test();
        consumer.consumerDynamicSubscribingTopic();
//        consumer.getKafkaTopic();
    }

}
