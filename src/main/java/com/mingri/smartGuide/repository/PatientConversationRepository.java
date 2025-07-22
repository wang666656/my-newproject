package com.mingri.smartGuide.repository;

import com.mingri.smartGuide.entity.PatientConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PatientConversationRepository extends JpaRepository<PatientConversation, UUID>, JpaSpecificationExecutor<PatientConversation> {
}