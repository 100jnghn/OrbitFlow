package com.finalproj.orbitflow.approval.document.file.entity;



import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.document.file.enums.ReferenceType;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결재 문서에 연결된 파일 정보를 관리하는 엔티티.
 * <p>
 * 하나의 Document에 업로드된 파일(File)을 연결하며,
 * 일반 첨부파일, 본문 이미지, 참조 문서 등
 * 파일의 용도에 따라 ReferenceType으로 구분한다.
 * <p>
 * 문서 재작성(반려 후 수정) 시에는 기존 문서에 연결된 파일 정보를 복사하여
 * 새로운 문서에 TEMP 상태로 다시 연결하는 데 사용된다.
 * <p>
 * 파일 자체의 저장 위치나 메타 정보는 File 엔티티가 담당하며,
 * 이 엔티티는 문서와 파일 간의 관계 및 문서 내 사용 맥락만 관리한다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFile
 * @since : 25. 12. 15. 월요일
 */


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

    @Column(name = "field_id", length = 100)
    private String fieldId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentFileStatus status = DocumentFileStatus.TEMP;

    public static DocumentFile copyFor(Document revised, DocumentFile documentFile) {
        return DocumentFile.builder()
                .document(revised)
                .file(documentFile.getFile())
                .referenceType(documentFile.getReferenceType())
                .referenceTargetId(documentFile.getReferenceTargetId())
                .referenceUrl(documentFile.getReferenceUrl())
                .fieldId(documentFile.getFieldId())
                .status(DocumentFileStatus.TEMP)
                .build();
    }

    public void updateStatus(DocumentFileStatus status) {
        this.status = status;
    }

    public boolean isImage() {
        return this.referenceType == ReferenceType.IMAGE;
    }

    public boolean isAttachment() {
        return this.referenceType == ReferenceType.ATTACHMENT;
    }}
