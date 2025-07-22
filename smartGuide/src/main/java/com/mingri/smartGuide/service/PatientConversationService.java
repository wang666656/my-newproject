package com.mingri.smartGuide.service;

import com.mingri.smartGuide.entity.PatientConversation;
import com.mingri.smartGuide.repository.ConversationRepository;
import com.mingri.smartGuide.repository.PatientConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientConversationService {
    @Autowired
    private PatientConversationRepository repository;

    @Autowired
    private ConversationRepository conversationRepository;

    public PatientConversation save(PatientConversation conversation) {
        return repository.save(conversation);
    }


    public Map<String, Object> findAllConversations(int page, int size, String keyword, String startTime, String endTime) {
        int realPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(realPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<PatientConversation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                Predicate idLike = cb.like(root.get("outpatientID"), "%" + keyword + "%");
                Predicate nameLike = cb.like(root.get("name"), "%" + keyword + "%");
                predicates.add(cb.or(idLike, nameLike));
            }
            if (startTime != null && !startTime.isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), parseDateTime(startTime, true)));
            }
            if (endTime != null && !endTime.isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), parseDateTime(endTime, false)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };


        Page<PatientConversation> pageResult = repository.findAll(spec, pageable);
        Map<String, Object> result = new HashMap<>();
        result.put("content", pageResult.getContent());
        result.put("totalElements", pageResult.getTotalElements());
        result.put("totalPages", pageResult.getTotalPages());
        result.put("page", realPage);
        return result;
    }

    private LocalDateTime parseDateTime(String input, boolean isStart) {
        if (input == null || input.isEmpty()) return null;
        try {
            // 优先尝试完整时间
            return LocalDateTime.parse(input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                // 尝试只传日期，自动补全时间
                LocalDate date = LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
                return isStart ? date.atStartOfDay() : date.atTime(23, 59, 59);
            } catch (DateTimeParseException ex) {
                // 还可以加更多格式支持
                return null;
            }
        }
    }
}