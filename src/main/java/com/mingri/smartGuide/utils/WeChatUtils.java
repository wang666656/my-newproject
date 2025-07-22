package com.mingri.smartGuide.utils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Random;

public class WeChatUtils {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new SecureRandom();

    /**
     * 生成随机字符串
     *
     * @param length 字符串长度，默认16
     * @return 随机字符串
     */
    public static String generateNonceStr(int length) {
        if (length <= 0) {
            length = 16;
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 生成当前时间戳（整数）
     *
     * @return 时间戳
     */
    public static long generateTimestamp() {
        return Instant.now().getEpochSecond();
    }
}