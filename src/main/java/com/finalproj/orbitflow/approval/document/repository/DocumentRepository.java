package com.finalproj.orbitflow.approval.document.repository;

import com.finalproj.orbitflow.approval.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentRepository
 * @since : 25. 12. 22. 월요일
 **/


public interface DocumentRepository extends JpaRepository<Document, Long>, DocumentRepositoryCustom {


    Page<Document> getDocumentByWriter_Id(Long writerId, Pageable pageable);

    Optional<Document> findTopByBeforeDocument_IdOrderByCreatedAtDesc(Long beforeDocumentId);

    boolean existsByBeforeDocument_Id(Long id);
}
