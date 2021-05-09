package com.startarget.sparkdemo.sparkCore.connector.file.write;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JavaIoWriter {
    public static void main(String[] args) throws IOException {
        String path="D:\\project\\SparkDemo\\src\\main\\java\\com\\startarget\\sparkdemo\\sparkCore\\connector\\file";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path+"bbb")));
        bufferedWriter.write("fsdfsad");
        bufferedWriter.flush();
        bufferedWriter.close();
    }
}
