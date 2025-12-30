package com.finalproj.orbitflow.approval.approvalLine.repository;

import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
