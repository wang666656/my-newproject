package com.mingri.smartGuide.service;

import com.mingri.smartGuide.entity.Conversation;
import com.mingri.smartGuide.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class NavigationService {

    private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);

    @Autowired
    private ConversationRepository conversationRepository;

    @Value("${wechat.appid}")
    private String appId;

    /**
     * 生成HIS小程序跳转链接
     *
     * @param outpatientId 门诊ID
     * @param name 患者姓名
     * @param targetPage 目标页面
     * @return 跳转链接和参数
     */
    public Map<String, Object> generateHisAppLink(String outpatientId, String name, String targetPage) {
        Map<String, Object> result = new HashMap<>();

        // 确定目标页面路径
        String path = determineTargetPath(targetPage);

        // 构建跳转参数
        Map<String, String> params = new HashMap<>();
        params.put("outpatientId", outpatientId);
        if (name != null && !name.trim().isEmpty()) {
            params.put("name", name);
        }

        result.put("success", true);
        result.put("appId", appId);
        result.put("path", path);
        result.put("params", params);

        return result;
    }

    /**
     * 根据目标页面名称确定实际路径
     *
     * @param targetPage 目标页面名称
     * @return 页面路径
     */
    private String determineTargetPath(String targetPage) {
        if (targetPage == null) {
            return "pages/index/index";
        }

        switch (targetPage) {
            case "appointment":
                return "pages/appointment/index";
            case "records":
                return "pages/medicalRecords/index";
            case "payment":
                return "pages/payment/index";
            case "registration":
                return "pages/registration/index";
            case "doctor":
                return "pages/doctorList/index";
            default:
                return "pages/index/index";
        }
    }

    /**
     * 记录访问信息
     *
     * @param outpatientId 门诊ID
     * @param source 来源
     * @param page 页面
     * @return 保存的访问记录
     */
    public Conversation recordVisit(String outpatientId, String source, String page) {
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        conversation.setOutpatientId(outpatientId);
        conversation.setSource(source != null ? source : "unknown");
        conversation.setPage(page != null ? page : "main");
        conversation.setVisitTime(LocalDateTime.now());
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        return conversationRepository.save(conversation);
    }
}