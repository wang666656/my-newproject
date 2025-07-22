package com.mingri.smartGuide.repository;

import com.mingri.smartGuide.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
}