package com.mingri.smartGuide.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WeChatConfig {
    private String appId;
    private String appSecret;
    private String frontEndUrl;
}