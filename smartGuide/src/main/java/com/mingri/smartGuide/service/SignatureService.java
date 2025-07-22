package com.mingri.smartGuide.service;

import com.mingri.smartGuide.config.WeChatConfig;
import com.mingri.smartGuide.utils.WeChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class SignatureService {

    private static final Logger logger = LoggerFactory.getLogger(SignatureService.class);

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private WeChatConfig weChatConfig;

    /**
     * 生成微信JS-SDK签名
     *
     * @param url 当前页面URL（不包含#及后面部分）
     * @return 包含签名参数的Map
     */
    public Map<String, String> generateSignature(String url) {
        if (url == null || url.trim().isEmpty()) {
            url = weChatConfig.getFrontEndUrl();
        }

        String jsapiTicket = weChatService.getJsapiTicket();
        String nonceStr = WeChatUtils.generateNonceStr(16);
        long timestamp = WeChatUtils.generateTimestamp();

        String signature = generateSignature(jsapiTicket, nonceStr, timestamp, url);

        Map<String, String> result = new HashMap<>();
        result.put("nonceStr", nonceStr);
        result.put("timestamp", String.valueOf(timestamp));
        result.put("url", url);
        result.put("signature", signature);
        result.put("appId", weChatConfig.getAppId());

        return result;
    }

    /**
     * 生成签名核心算法
     */
    private String generateSignature(String jsapiTicket, String nonceStr, long timestamp, String url) {
        String[] params = {
                "jsapi_ticket=" + jsapiTicket,
                "noncestr=" + nonceStr,
                "timestamp=" + timestamp,
                "url=" + url
        };

        logger.info("当前使用的 jsapi_ticket: {}", jsapiTicket);
        logger.info("当前使用的 noncestr: {}", nonceStr);
        logger.info("当前使用的 timestamp: {}", timestamp);
        logger.info("当前使用的 url: {}", url);
        // 按字典序排序
        Arrays.sort(params);

        // 拼接字符串
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                stringBuilder.append("&");
            }
            stringBuilder.append(params[i]);
        }

        String string1 = stringBuilder.toString();

        // SHA1签名
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-test-soap-response.xml");
            digest.update(string1.getBytes());
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}