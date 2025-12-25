package com.finalproj.orbitflow.approval.approvalLine.repository;

import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalLineRepository
 * @since : 25. 12. 25. 목요일
 **/


public interface ApprovalLineRepository extends JpaRepository<ApprovalLine, Long> {

    void deleteByDocumentAndStatus(Document document, ApprovalStatus approvalStatus);
}
