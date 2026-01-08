package com.finalproj.orbitflow.approval.documentSignature.repository;

import com.finalproj.orbitflow.approval.documentSignature.entity.DocumentSignature;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentSignature
 * @since : 26. 1. 8. 목요일
 **/


public interface DocumentSignatureRepository extends JpaRepository<DocumentSignature, Long> {
    boolean existsByDocument_IdAndApprovalLine_Id(Long id, Long id1);
}
