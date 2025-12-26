package com.finalproj.orbitflow.message.repository;

import com.finalproj.orbitflow.message.entity.MessageRecipient;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, Long> {

    Page<MessageRecipient> findByCompanyIdAndRecipient_IdAndFolderTypeAndDeletedAtIsNull(
            Long companyId,
            Long employeeId,
            MessageFolderType folderType,
            Pageable pageable
    );

    Optional<MessageRecipient> findByCompanyIdAndMessage_IdAndRecipient_IdAndDeletedAtIsNull(
            Long companyId,
            Long messageId,
            Long employeeId
    );
}
