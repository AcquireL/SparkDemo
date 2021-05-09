package com.startarget.sparkdemo.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Md5Test {
    final public static char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f'};

    /**
     * @param content 文本内容
     * @return md5值
     */
    public static String md5Generate(String content) {
        int i = 0;
        char[] ret = new char[32];
        try {
            byte[] digest = MessageDigest.getInstance("MD5")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            for (byte b : digest) {
                ret[i++] = HEX_CHAR[b >>> 4 & 0xF];
                ret[i++] = HEX_CHAR[b & 0xF];
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new String(ret);
    }

    public static void main(String[] args) {
        System.out.println(Md5Test.md5Generate("cccc"));
    }
}
