package com.startarget.sparkdemo.test;

import org.apache.hadoop.fs.Stat;

public class StaticTest {
    static int a = 0;

    public static void main(String[] args) {
        StaticTest staticTest = new StaticTest();
        staticTest.a++;
        StaticTest staticTest1 = new StaticTest();
        staticTest1.a++;
        staticTest = new StaticTest();
        staticTest.a++;
        StaticTest.a++;
        System.out.printf("a:" +a);
    }
}
