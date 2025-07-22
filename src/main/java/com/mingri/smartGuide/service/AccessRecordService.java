package com.mingri.smartGuide.service;

import com.mingri.smartGuide.entity.AccessRecord;
import com.mingri.smartGuide.repository.AccessRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccessRecordService {

    private static final Logger log = LoggerFactory.getLogger(AccessRecordService.class);

    @Autowired
    private AccessRecordRepository accessRecordRepository;

    /**
     * 记录访问（仅保留必要参数）
     */
    public AccessRecord recordAccess(String outpatientID) {
        try {
            AccessRecord accessRecord = new AccessRecord();
            accessRecord.setOutpatientID(outpatientID);
            // 显式设置访问时间，确保覆盖@PrePersist可能的失效场景
            accessRecord.setAccessTime(LocalDateTime.now());

            log.info("准备保存访问记录：outpatientID={}, accessTime={}",
                    outpatientID, accessRecord.getAccessTime());

            AccessRecord savedRecord = accessRecordRepository.save(accessRecord);
            log.info("访问记录保存成功，ID：{}", savedRecord.getId());
            return savedRecord;
        } catch (Exception e) {
            log.error("访问记录保存失败：outpatientID={}", outpatientID, e);
            throw new RuntimeException("数据库操作失败：" + e.getMessage(), e);
        }
    }

    // 以下方法保持不变（分页查询和统计）
    public Map<String, Object> findAllAccessRecords(int page, int size, String outpatientID, String startTime, String endTime) {
        page = Math.max(0, page);
        size = Math.min(Math.max(1, size), 1000);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "accessTime"));

        Specification<AccessRecord> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (outpatientID != null && !outpatientID.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("outpatientID"), outpatientID.trim()));
            }

            if (startTime != null && !startTime.trim().isEmpty()) {
                try {
                    LocalDateTime start = LocalDateTime.parse(
                            startTime + " 00:00:00",
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    );
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("accessTime"), start));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("开始时间格式错误，正确格式：yyyy-MM-dd");
                }
            }

            if (endTime != null && !endTime.trim().isEmpty()) {
                try {
                    LocalDateTime end = LocalDateTime.parse(
                            endTime + " 23:59:59",
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    );
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("accessTime"), end));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("结束时间格式错误，正确格式：yyyy-MM-dd");
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<AccessRecord> pageResult = accessRecordRepository.findAll(spec, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("content", pageResult.getContent());
        result.put("totalElements", pageResult.getTotalElements());
        result.put("totalPages", pageResult.getTotalPages());
        result.put("currentPage", page);
        result.put("size", size);

        return result;
    }

    public Map<String, Object> getAccessStatistics(String outpatientID) {
        Map<String, Object> result = new HashMap<>();

        if (outpatientID != null && !outpatientID.trim().isEmpty()) {
            Long userAccessCount = accessRecordRepository.countByOutpatientID(outpatientID.trim());
            List<AccessRecord> userRecentAccess = accessRecordRepository.findByOutpatientIDOrderByAccessTimeDesc(outpatientID.trim());

            result.put("userAccessCount", userAccessCount);
            result.put("userRecentAccess", userRecentAccess.size() > 10 ? userRecentAccess.subList(0, 10) : userRecentAccess);
        }

        Long totalAccessCount = accessRecordRepository.countTotalAccess();
        result.put("totalAccessCount", totalAccessCount);

        return result;
    }
}