package com.finalproj.orbitflow.message.repository;

import com.finalproj.orbitflow.message.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByIdAndCompanyId(Long id, Long companyId);

    Page<Message> findByCompanyIdAndSender_IdOrderByCreatedAtDesc(
            Long companyId,
            Long senderId,
            Pageable pageable
    );
}
