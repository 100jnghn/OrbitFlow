package com.finalproj.orbitflow.approval.document.ai.repository;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.ai.entity.DocumentAISummary;
import com.finalproj.orbitflow.approval.document.ai.enums.SummaryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentAiSummaryRepository
 * @since : 26. 1. 5. 월요일
 **/


public interface DocumentAiSummaryRepository extends JpaRepository<DocumentAISummary, Long> {

    Optional<DocumentAISummary> findByDocumentAndSummaryType(Document document, SummaryType summaryType);

    Optional<DocumentAISummary> findByDocumentAndBeforeDocumentAndSummaryType(Document document, Document beforeDocument, SummaryType summaryType);
}
