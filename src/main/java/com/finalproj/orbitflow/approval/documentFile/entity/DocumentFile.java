package com.finalproj.orbitflow.approval.documentFile.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : DocumentFile
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.documentFile.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.documentFile.enums.ReferenceType;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "document_file",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_df_document_file",
                        columnNames = {"document_id", "file_id"}
                ),
                @UniqueConstraint(
                        name = "uk_df_document_reference",
                        columnNames = {"document_id", "reference_type", "reference_target_id"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 20)
    private ReferenceType referenceType;

    @Column(name = "reference_target_id")
    private Long referenceTargetId;

    @Column(name = "reference_url", length = 255)
    private String referenceUrl;

    //TODO
    // TEMP + 7일 경과 → S3 삭제 + DB 정리
    @Column
    private DocumentFileStatus status = DocumentFileStatus.TEMP;

    public void updateStatus(DocumentFileStatus status) {
        this.status = status;
    }
}
