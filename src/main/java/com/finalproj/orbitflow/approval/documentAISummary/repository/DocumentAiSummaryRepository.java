package com.finalproj.orbitflow.approval.documentAISummary.repository;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.documentAISummary.entity.DocumentAISummary;
import com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryType;
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
}
