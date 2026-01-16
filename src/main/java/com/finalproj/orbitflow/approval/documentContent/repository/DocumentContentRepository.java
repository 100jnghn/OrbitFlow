package com.finalproj.orbitflow.approval.documentContent.repository;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentRepository
 * @since : 25. 12. 26. 금요일
 **/


public interface DocumentContentRepository extends JpaRepository<DocumentContent, Long> {

    Optional<DocumentContent> findByDocument_Id(Long documentId);

    void deleteByDocument(Document draft);
}
