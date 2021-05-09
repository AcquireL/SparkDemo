package com.startarget.sparkdemo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  将时间戳转化为日期格式
 */
public class TimestampToDate {
    public static void main(String[] args) {
        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
        Date date = new Date(1612258139105L);
        String format = df.format(date);
        System.out.println(format);

        System.out.println(new Date().getTime());
    }
}
