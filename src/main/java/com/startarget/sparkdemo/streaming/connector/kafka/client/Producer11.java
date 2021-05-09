package com.startarget.sparkdemo.streaming.connector.kafka.client;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Producer11 {

    public static void producer() {
        String topic = "VDF_TEST_3";
        String broker_list="learn:9092";
        Properties props = new Properties();
        props.put("bootstrap.servers", broker_list);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer producer = new KafkaProducer<String, String>(props);
        for (int i = 1; i <= 20; i++) {
            // BusinessEntity bs = new BusinessEntity("business5", "业务5");
            // String msg="{\"value\":{\"mid\":\"d32707d8-3f7e-4795-9d57-de9957988f0b\",\"did\":\"0593ce2837c245032910da1edd3a8a9e\",\"aid\":\"1234\",\"sid\":\"123456\",\"svid\":\"2.0\",\"ts\":\"1"+i+"591189249159\",\"d\":{\"G1\":\"d32707d8-3f7e-4795-9d57-de9957988f0b\",\"G2\":\"0593ce2837c245032910da1edd3a8a9e\",\"G3\":\"1591189249156\",\"G4\":\"123456\"}}}";
            String msg="{\"value\":{\"mid\":\"d32707d8-3f7e-4795-9d57-de9957988f0b\",\"did\":\"0593ce2837c245032910da1edd3a8a9e\",\"aid\":\"1\",\"sid\":\"1\",\"svid\":\"1\",\"ts\":\"15911"+i+"9249159\",\"d\":{\"G1\":\"d32707d8-3f7e-4795-9d57-de9957988f0b\",\"G2\":\"0593ce2837c245032910da1edd3a8a9e\",\"G4\":\"1591189249156\"}}}";
            //KafkaDataEntity data=new KafkaDataEntity("topic"+i,msg);
            ProducerRecord record = new ProducerRecord<String, String>(
                    topic,
                    0,
                    null,
                    msg);
            producer.send(record);
            System.out.println("发送数据: " + msg);
        }
        producer.flush();
    }

    public static void main(String[] args) {
        producer();
    }
}


