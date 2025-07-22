package com.mingri.smartGuide.controller;

import com.mingri.smartGuide.entity.Feedback;
import com.mingri.smartGuide.entity.PatientConversation;
import com.mingri.smartGuide.repository.FeedbackRepository;
import com.mingri.smartGuide.repository.PatientConversationRepository;
import com.mingri.smartGuide.service.PatientConversationService;
import com.mingri.smartGuide.service.TracingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PatientConversationController {

    @Autowired
    private PatientConversationRepository repository;

    @Autowired
    private PatientConversationService service;

    @Autowired
    private TracingService tracingService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @PostMapping("/savePatientInfo")
    public ResponseEntity<?> saveConversation(@RequestBody PatientConversation conversation) {
        Map<String, Object> result = new HashMap<>();
        if (conversation.getConversationID() == null || conversation.getConversationID().toString().trim().isEmpty()) {
            result.put("message", "会话ID不能为空");
            result.put("data", conversation);
            return ResponseEntity.badRequest().body(result);
        }
        if (conversation.getOutpatientID() == null || conversation.getOutpatientID().trim().isEmpty()) {
            result.put("message", "用户ID不能为空");
            result.put("data", conversation);
            return ResponseEntity.badRequest().body(result);
        }
        if (conversation.getName() == null || conversation.getName().trim().isEmpty()) {
            result.put("message", "姓名不能为空");
            result.put("data", conversation);
            return ResponseEntity.badRequest().body(result);
        }
        if (conversation.getAge() == null) {
            result.put("message", "年龄不能为空");
            result.put("data", conversation);
            return ResponseEntity.badRequest().body(result);
        }
        int age = Integer.parseInt(conversation.getAge().toString().trim());
        if (age <= 0 || age >= 200) {
            result.put("message", "年龄必须为大于0且小于200的数字");
            result.put("data", conversation);
            return ResponseEntity.badRequest().body(result);
        }
        if (conversation.getGender() == null || conversation.getGender().trim().isEmpty()) {
            result.put("message", "性别不能为空");
            result.put("data", conversation);
            return ResponseEntity.badRequest().body(result);
        }
        String gender = conversation.getGender().trim();
        if (!gender.equals("男") && !gender.equals("女")) {
            result.put("message", "性别必须为男或者女");
            result.put("data", conversation);
            return ResponseEntity.badRequest().body(result);
        }

        PatientConversation saved = repository.save(conversation);
        result.put("message", "保存成功");
        result.put("data", saved);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/conversationList")
    public ResponseEntity<?> getConversationList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Map<String, Object> result = service.findAllConversations(page, size, keyword, startTime, endTime);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        List<?> content = (List<?>) result.get("content");
        if (content == null || content.isEmpty()) {
            result.put("message", "没有查到数据");
        } else {
            result.put("message", "");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/tracing")
    public List<Map<String, Object>> getTracing(@RequestParam String conversationID) {
        return tracingService.getTracing(conversationID);
    }

    @PostMapping("/saveFeedback")
    public ResponseEntity<?> saveFeedback(@RequestBody Feedback feedback) {
        Map<String, Object> result = new HashMap<>();

        if (feedback.getMessageID() == null || feedback.getMessageID().toString().trim().isEmpty()) {
            result.put("message", "消息ID不能为空");
            result.put("data", feedback);
            return ResponseEntity.badRequest().body(result);
        }
        if (feedback.getConversationID() == null || feedback.getConversationID().toString().trim().isEmpty()) {
            result.put("message", "会话ID不能为空");
            result.put("data", feedback);
            return ResponseEntity.badRequest().body(result);
        }
        if (feedback.getOutpatientID() == null || feedback.getOutpatientID().trim().isEmpty()) {
            result.put("message", "用户ID不能为空");
            result.put("data", feedback);
            return ResponseEntity.badRequest().body(result);
        }
        if (feedback.getFeedBack() == null) {
            result.put("message", "反馈状态不能为空");
            result.put("data", feedback);
            return ResponseEntity.badRequest().body(result);
        }

        Feedback saved = feedbackRepository.save(feedback);
        result.put("message", "反馈保存成功");
        result.put("data", saved);
        return ResponseEntity.ok(result);
    }
}
