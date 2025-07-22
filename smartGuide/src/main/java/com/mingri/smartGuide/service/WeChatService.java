package com.mingri.smartGuide.service;

import com.mingri.smartGuide.config.WeChatConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeChatService {
    private static final Logger logger = LoggerFactory.getLogger(WeChatService.class);
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
    // 声明RestTemplate字段
    private final RestTemplate restTemplate;
    // 缓存access_token和jsapi_ticket
    private final Map<String, CacheItem> cache = new ConcurrentHashMap<>();
    @Autowired
    private WeChatConfig weChatConfig;

    @Autowired
    public WeChatService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 获取access_token
     *
     * @return access_token
     */
    public String getAccessToken() {
        return getFromCache("access_token",
                () -> {
                    if (weChatConfig == null || weChatConfig.getAppId() == null || weChatConfig.getAppSecret() == null) {
                        throw new IllegalStateException("WeChatConfig is not properly initialized");
                    }

                    Map<String, String> params = new HashMap<>();
                    params.put("grant_type", "client_credential");
                    params.put("appid", weChatConfig.getAppId());
                    params.put("secret", weChatConfig.getAppSecret());

                    try {
                        ResponseEntity<Map> response = restTemplate.getForEntity(ACCESS_TOKEN_URL + "?grant_type={grant_type}&appid={appid}&secret={secret}", Map.class, params);
                        Map<String, Object> body = response.getBody();

                        if (body == null) {
                            logger.error("Empty response from WeChat API");
                            throw new RuntimeException("Empty response from WeChat API");
                        }

                        // 检查错误信息
                        if (body.containsKey("errcode") && !body.get("errcode").equals(0)) {
                            String errorCode = body.get("errcode").toString();
                            String errorMsg = body.get("errmsg").toString();
                            logger.error("WeChat API error: [{}] {}", errorCode, errorMsg);
                            throw new RuntimeException(String.format("WeChat API error: [%s] %s", errorCode, errorMsg));
                        }

                        if (!body.containsKey("access_token") || !body.containsKey("expires_in")) {
                            logger.error("Invalid response from WeChat API: {}", body);
                            throw new RuntimeException("Invalid response from WeChat API: Missing 'access_token' or 'expires_in'");
                        }

                        String accessToken = (String) body.get("access_token");
                        int expiresIn = (int) body.get("expires_in");
                        return new AccessTokenResult(accessToken, expiresIn);
                    } catch (Exception e) {
                        logger.error("Error retrieving access_token: {}", e.getMessage(), e);
                        throw e;
                    }
                });
    }

    /**
     * 获取jsapi_ticket
     *
     * @return jsapi_ticket
     */
    public String getJsapiTicket() {
        return getFromCache("jsapi_ticket",
                () -> {
                    String accessToken = getAccessToken();
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", accessToken);
                    params.put("type", "jsapi");

                    try {
                        ResponseEntity<Map> response = restTemplate.getForEntity(JSAPI_TICKET_URL + "?access_token={access_token}&type={type}", Map.class, params);
                        Map<String, Object> body = response.getBody();

                        if (body == null) {
                            logger.error("Empty response from WeChat API for jsapi_ticket");
                            throw new RuntimeException("Empty response from WeChat API for jsapi_ticket");
                        }

                        // 检查错误信息
                        if (body.containsKey("errcode") && !body.get("errcode").equals(0)) {
                            String errorCode = body.get("errcode").toString();
                            String errorMsg = body.get("errmsg").toString();
                            logger.error("WeChat API error for jsapi_ticket: [{}] {}", errorCode, errorMsg);
                            throw new RuntimeException(String.format("WeChat API error for jsapi_ticket: [%s] %s", errorCode, errorMsg));
                        }

                        if (!body.containsKey("ticket") || !body.containsKey("expires_in")) {
                            logger.error("Invalid response from WeChat API for jsapi_ticket: {}", body);
                            throw new RuntimeException("Invalid response from WeChat API for jsapi_ticket: Missing 'ticket' or 'expires_in'");
                        }

                        String jsapiTicket = (String) body.get("ticket");
                        int expiresIn = (int) body.get("expires_in");
                        return new JsapiTicketResult(jsapiTicket, expiresIn);
                    } catch (Exception e) {
                        logger.error("Error retrieving jsapi_ticket: {}", e.getMessage(), e);
                        throw e;
                    }
                });
    }

    /**
     * 从缓存获取或更新数据
     */
    private <T extends TokenResult> String getFromCache(String key, TokenSupplier<T> supplier) {
        CacheItem cacheItem = cache.get(key);
        long now = System.currentTimeMillis();

        if (cacheItem != null && now < cacheItem.expireTime) {
            logger.info("从缓存获取" + key);
            return cacheItem.value;
        }

        logger.info("更新" + key);
        T result = supplier.get();

        if (result == null || result.getValue() == null || result.getExpiresIn() <= 0) {
            throw new RuntimeException("Invalid result from supplier for key: " + key);
        }

        long expireTime = now + (result.getExpiresIn() - 300) * 1000L; // 提前5分钟过期
        cacheItem = new CacheItem(result.getValue(), expireTime);
        cache.put(key, cacheItem);

        return result.getValue();
    }

    private interface TokenSupplier<T extends TokenResult> {
        T get();
    }

    private interface TokenResult {
        String getValue();

        int getExpiresIn();
    }

    private static class AccessTokenResult implements TokenResult {
        private final String accessToken;
        private final int expiresIn;

        public AccessTokenResult(String accessToken, int expiresIn) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
        }

        @Override
        public String getValue() {
            return accessToken;
        }

        @Override
        public int getExpiresIn() {
            return expiresIn;
        }
    }

    private static class JsapiTicketResult implements TokenResult {
        private final String ticket;
        private final int expiresIn;

        public JsapiTicketResult(String ticket, int expiresIn) {
            this.ticket = ticket;
            this.expiresIn = expiresIn;
        }

        @Override
        public String getValue() {
            return ticket;
        }

        @Override
        public int getExpiresIn() {
            return expiresIn;
        }
    }

    private static class CacheItem {
        final String value;
        final long expireTime;

        public CacheItem(String value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
    }
}