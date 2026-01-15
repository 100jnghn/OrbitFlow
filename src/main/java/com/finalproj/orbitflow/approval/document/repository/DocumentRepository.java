package com.finalproj.orbitflow.approval.document.repository;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
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


    @Query("""
                select count(d)
                from Document d
                where d.writer.id = :employeeId
                  and d.status = :status
            """)
    int countByWriterAndStatus(
            @Param("employeeId") Long employeeId,
            @Param("status") DocumentStatus status
    );

    @Query("""
    select count(d)
    from Document d
    where d.writer.id = :employeeId
      and d.status = :status
      and d.submittedAt >= :start
      and d.submittedAt < :end
    """)
    int countByWriterAndStatusBetween(
            @Param("employeeId") Long employeeId,
            @Param("status") DocumentStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );


    @Query("""
    select count(d)
    from Document d
    where d.writer.id = :employeeId
      and d.status = :status
      and not exists (
          select 1
          from Document d2
          where d2.beforeDocument.id = d.id
      )
    """)
    int countRejectedNotResubmitted(
            @Param("employeeId") Long employeeId,
            @Param("status") DocumentStatus status
    );

    @Query("""

            select count(d)
    from Document d
    where d.writer.id = :employeeId
      and d.status = :status
      and d.submittedAt >= :startInstant
    """)
    int countByWriterAndStatusFromDate(
            @Param("employeeId") Long employeeId,
            @Param("status") DocumentStatus status,
            @Param("startInstant") Instant startInstant
    );

}
