package com.finalproj.orbitflow.chatbot.chatbot.repository;

import com.finalproj.orbitflow.chatbot.chatbot.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatConversationRepository
 * @since : 2026. 1. 6. 화요일
 */
public interface ChatConversationRepository extends JpaRepository<ChatConversation,String> {

    Optional<ChatConversation> findByIdAndCompanyIdAndEmployeeIdAndDeletedFalse(Long id, Long companyId, Long employeeId);

    List<ChatConversation> findTop20ByCompanyIdAndEmployeeIdAndDeletedFalseOrderByUpdatedAtDesc(Long companyId, Long employeeId);
}
