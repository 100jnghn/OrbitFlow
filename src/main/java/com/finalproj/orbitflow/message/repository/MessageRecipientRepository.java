package com.finalproj.orbitflow.message.repository;

import com.finalproj.orbitflow.message.entity.MessageRecipient;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, Long>, JpaSpecificationExecutor<MessageRecipient> {

    /** (받은/보낸) 기본 목록: 보관함 제외 + 삭제 제외 */
    Page<MessageRecipient> findByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedFalseAndMessageFolderTypeOrderByCreatedAtDesc(
            Long companyId,
            Long employeeId,
            MessageFolderType folderType,
            Pageable pageable
    );

    /** 보관함 목록: isArchived=true + 삭제 제외 (folderType은 복귀용이라 필터 걸지 않는 걸 추천) */
    Page<MessageRecipient> findByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedTrueOrderByCreatedAtDesc(
            Long companyId,
            Long employeeId,
            Pageable pageable
    );

    /** 상세 조회용(내 것만) */
    Optional<MessageRecipient> findByCompanyIdAndMessage_IdAndEmployee_IdAndDeletedAtIsNull(
            Long companyId,
            Long messageId,
            Long employeeId
    );

    /** 중복 방지(옵션) */
    boolean existsByCompanyIdAndMessage_IdAndEmployee_Id(Long companyId, Long messageId, Long employeeId);
}
