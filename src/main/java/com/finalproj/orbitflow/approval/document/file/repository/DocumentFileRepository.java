package com.finalproj.orbitflow.approval.document.file.repository;

import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.document.file.enums.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
    
            case
                when df.referenceTargetId is not null
                    then refDoc.title
                else f.originFile
            end as displayName,
    
            case
                when df.referenceTargetId is not null
                    then writer.name
                else null
            end as writerName,
    
            f.fileSize as fileSize,
            df.referenceTargetId as referenceTargetId,
            df.referenceType as referenceType,
            df.status as status,
            df.fieldId as fieldId
        from DocumentFile df
        join df.file f
        left join Document refDoc
            on df.referenceTargetId = refDoc.id
        left join refDoc.writer writer
        where df.document.id = :documentId
        order by df.createdAt asc
        """)
    List<DocumentFileAttachedProjection> findAttachedFilesByDocumentId(
            @Param("documentId") Long documentId
    );





    List<DocumentFile> findByDocument_Id(Long documentId);

    Optional<DocumentFile> findByDocument_IdAndFile_Id(Long documentId, Long fileId);

    long countByFile_Id(Long id);

    @Query("""
    select df
    from DocumentFile df
    where df.document.id = :documentId
      and df.referenceType = :referenceType
      and df.referenceTargetId is null
      and df.status = :status
    """)
    Optional<DocumentFile> findDocumentFileByDocumentAndType(
            @Param("documentId") Long documentId,
            @Param("referenceType") ReferenceType referenceType,
            @Param("status") DocumentFileStatus status
    );

    Optional<DocumentFile> findByDocument_IdAndReferenceTypeAndReferenceTargetIdIsNullAndStatus(
            Long documentId,
            ReferenceType referenceType,
            DocumentFileStatus status
    );
}
