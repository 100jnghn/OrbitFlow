package com.finalproj.orbitflow.approval.document.content.repository;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.content.entity.DocumentContent;
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
