package com.finalproj.orbitflow.approval.documentFile.repository;

import com.finalproj.orbitflow.approval.documentFile.entity.DocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFileRepository
 * @since : 26. 1. 2. 금요일
 **/


public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {


    @Query("""
    select
        df.id as documentFileId,
        f.id as fileId,
        f.originFile as fileName,
        f.fileSize as fileSize,
        df.referenceTargetId as referenceTargetId,
        df.status as status
    from DocumentFile df
    join df.file f
    where df.document.id = :documentId
    order by df.createdAt asc
    """)
    List<DocumentFileAttachedProjection> findAttachedFilesByDocumentId(
            @Param("documentId") Long documentId
    );

    List<DocumentFile> findByDocument_Id(Long documentId);
}
