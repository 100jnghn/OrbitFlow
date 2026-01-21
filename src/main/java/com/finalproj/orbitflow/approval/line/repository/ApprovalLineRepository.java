package com.finalproj.orbitflow.approval.line.repository;

import com.finalproj.orbitflow.approval.line.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.line.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalLineRepository
 * @since : 25. 12. 25. 목요일
 **/


public interface ApprovalLineRepository extends JpaRepository<ApprovalLine, Long> {

    int deleteByDocumentAndStatus(Document document, ApprovalStatus approvalStatus);


    List<ApprovalLine> findByDocument_IdOrderByOrderNoAsc(Long documentId);


    Optional<ApprovalLine> findFirstByDocumentAndStatusOrderByOrderNoAsc(Document document, ApprovalStatus approvalStatus);

    boolean existsByDocumentIdAndApproverId(Long id, Long employeeId);

    Optional<ApprovalLine> findByDocument_IdAndStatus(Long documentId, ApprovalStatus approvalStatus);


    void deleteByDocument(Document draft);

    @Query("""
    select count(distinct al.document.id)
    from ApprovalLine al
    where al.approver.id = :employeeId
      and al.document.status = :documentStatus
    """)
    int countMyTurnWaiting(
            @Param("employeeId") Long employeeId,
            @Param("documentStatus") DocumentStatus documentStatus
    );

    @Query("""
    SELECT COUNT(al)
    FROM ApprovalLine al
    JOIN al.document d
    WHERE al.approver.id = :employeeId
      AND al.status = :waitingStatus
      AND d.status = :documentStatus
    """)
    int countWaitingBeforeMyTurn(
            @Param("employeeId") Long employeeId,
            @Param("waitingStatus") ApprovalStatus waitingStatus,
            @Param("documentStatus") DocumentStatus documentStatus
    );

}
