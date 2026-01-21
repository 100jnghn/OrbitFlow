package com.finalproj.orbitflow.message.repository;

import com.finalproj.orbitflow.message.entity.MessageRecipient;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MessageRecipientRepository
                extends JpaRepository<MessageRecipient, Long>, JpaSpecificationExecutor<MessageRecipient> {

        /** (받은/보낸) 기본 목록: 보관함 제외 + 삭제 제외 */
        Page<MessageRecipient> findByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedFalseAndMessageFolderTypeOrderByCreatedAtDesc(
                        Long companyId,
                        Long employeeId,
                        MessageFolderType folderType,
                        Pageable pageable);

        /** 보관함 목록: isArchived=true + 삭제 제외 (folderType은 복귀용이라 필터 걸지 않는 걸 추천) */
        Page<MessageRecipient> findByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedTrueOrderByCreatedAtDesc(
                        Long companyId,
                        Long employeeId,
                        Pageable pageable);

        /** 상세 조회용(내 것만) */
        Optional<MessageRecipient> findByCompanyIdAndMessage_IdAndEmployee_IdAndDeletedAtIsNull(
                        Long companyId,
                        Long messageId,
                        Long employeeId);

        /** 중복 방지(옵션) */
        boolean existsByCompanyIdAndMessage_IdAndEmployee_Id(Long companyId, Long messageId, Long employeeId);

        /** 메시지 수신자 목록 조회 (삭제 여부 상관없이 조회 - 발신자가 상세에서 확인하기 위함) */
        List<MessageRecipient> findByMessage_IdAndMessageFolderType(
                        Long messageId,
                        MessageFolderType folderType);

        /** 메시지 수신자 목록 조회 (보낸 메시지함에서 수신자 정보 표시용) */
        List<MessageRecipient> findByMessage_IdAndMessageFolderTypeAndDeletedAtIsNull(
                        Long messageId,
                        MessageFolderType folderType);

        /** [최적화] 여러 메시지의 수신자 목록을 한 번에 조회 */
        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "employee", "message", "message.sender",
                        "message.files" })
        List<MessageRecipient> findByMessage_IdInAndMessageFolderTypeAndDeletedAtIsNull(
                        List<Long> messageIds,
                        MessageFolderType folderType);

        /** 안 읽은 메시지 카운트 (받은 메시지함 기준) */
        long countByCompanyIdAndEmployee_IdAndDeletedAtIsNullAndIsArchivedFalseAndMessageFolderTypeAndIsReadFalse(
                        Long companyId,
                        Long employeeId,
                        MessageFolderType folderType);

        /** 메시지의 활성 recipient 수 조회 (파일 삭제 여부 판단용) */
        long countByMessage_IdAndDeletedAtIsNull(Long messageId);
}
