package com.mingri.smartGuide.controller;

import com.mingri.smartGuide.service.SignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WeChatSignatureController {
    private static final Logger logger = LoggerFactory.getLogger(WeChatSignatureController.class);
    @Autowired
    private SignatureService signatureService;

    /**
     * 生成微信JS-SDK签名接口
     *
     * @param url 当前页面URL，可选，默认使用配置中的URL
     * @return 签名参数
     */
    @PostMapping("/api/wechat/signature")
    public ResponseEntity<Map<String, String>> generateSignature(@RequestBody Map<String, String> requestBody) {
        String url = requestBody.get("url");
        logger.info("url:" + url);
        Map<String, String> signature = signatureService.generateSignature(url);
        return ResponseEntity.ok(signature);
    }
}