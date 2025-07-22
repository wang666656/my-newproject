package com.mingri.smartGuide.controller;

import com.mingri.smartGuide.entity.AccessRecord;
import com.mingri.smartGuide.service.AccessRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/access")
public class AccessRecordController {

    @Autowired
    private AccessRecordService accessRecordService;

    /**
     * 记录访问（仅保留 outpatientID）
     */
    @PostMapping("/record")
    public ResponseEntity<?> recordAccess(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();

        // 仅校验 outpatientID（核心参数）
        String outpatientID = requestBody.get("outpatientID");
        if (outpatientID == null || outpatientID.trim().isEmpty()) {
            result.put("message", "用户ID不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            // 调用Service层，仅传递 outpatientID（不再传递ip和userAgent）
            AccessRecord accessRecord = accessRecordService.recordAccess(outpatientID.trim());
            result.put("message", "访问记录保存成功");
            result.put("data", accessRecord);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("message", "访问记录保存失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 查询访问记录列表（逻辑不变，仅处理保留的三个字段）
     */
    @GetMapping("/list")
    public ResponseEntity<?> getAccessRecordList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String outpatientID,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        // 参数校验：限制每页最大条数
        if (size > 1000) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("message", "每页大小不能超过1000");
            return ResponseEntity.badRequest().body(errorResult);
        }

        // 调用Service查询（返回结果仅包含id、outpatientID、accessTime）
        Map<String, Object> result = accessRecordService.findAllAccessRecords(page, size, outpatientID, startTime, endTime);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        List<?> content = (List<?>) result.get("content");
        if (content == null || content.isEmpty()) {
            result.put("message", "没有查到数据");
        } else {
            result.put("message", "查询成功");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取访问统计信息（逻辑不变）
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getAccessStatistics(@RequestParam(required = false) String outpatientID) {
        Map<String, Object> result = accessRecordService.getAccessStatistics(outpatientID);
        result.put("message", "统计信息获取成功");
        return ResponseEntity.ok(result);
    }
}