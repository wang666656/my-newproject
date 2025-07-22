package com.mingri.smartGuide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TracingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getTracing(String conversationID) {
        String sql = "SELECT " +
                "wne.inputs, " +
                "wne.outputs, " +
                "wne.process_data, " +
                "wne.status, " +
                "wne.error, " +
                "m.query, " +
                "m.answer " +
                "FROM conversations c " +
                "JOIN messages m ON c.id = m.conversation_id " +
                "LEFT JOIN workflow_runs wr ON m.workflow_run_id = wr.id " +
                "LEFT JOIN workflow_node_executions wne ON wr.id = wne.workflow_run_id " +
                "WHERE c.id = ?::uuid " +
                "AND m.workflow_run_id IS NOT NULL " +
                "ORDER BY wne.created_at ASC";
        return jdbcTemplate.queryForList(sql, conversationID);
    }
}