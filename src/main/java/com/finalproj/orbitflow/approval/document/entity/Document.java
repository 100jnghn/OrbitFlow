package com.finalproj.orbitflow.approval.document.entity;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.form.template.entity.FormTemplate;
import com.finalproj.orbitflow.approval.form.template.group.entity.FormTemplateGroup;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;


/**
 * 결재 문서 자체를 표현하는 엔티티.
 * <p>
 * 하나의 문서는 특정 회사와 양식 그룹에 속하며,
 * 작성자, 제목, 상태 정보를 기준으로 결재 흐름을 따라 이동한다.
 * <p>
 * 문서는 DRAFT 상태에서 작성된 뒤 상신되며,
 * 결재 진행 결과에 따라 APPROVED 또는 REJECTED 상태로 변경된다.
 * <p>
 * 반려된 문서는 beforeDocument를 통해 이전 문서를 참조하며,
 * 이를 기반으로 재기안 문서가 생성될 수 있다.
 * <p>
 * 이 엔티티는 문서의 기본 정보와 상태만 관리하고,
 * 실제 문서 내용(JSON), 첨부파일, 결재선 등은
 * 별도의 엔티티에서 분리하여 관리한다.
 *
 * @author : Choi MinHyeok
 * @filename : Document
 * @since : 25. 12. 15. 월요일
 */


@Entity
@Table(name = "document")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_group_id", nullable = false)
    private FormTemplateGroup templateGroup;

    @Column(name = "template_version", nullable = false)
    private int templateVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private Employee writer;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "before_document_id")
    private Document beforeDocument;

    @OneToMany(mappedBy = "beforeDocument")
    private List<Document> revisedDocuments;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "submitted_at", nullable = true)
    private Instant submittedAt;

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateStatus(DocumentStatus status) {
        this.status = status;
    }

    public void submit() {
        this.status = DocumentStatus.IN_PROGRESS;
        this.submittedAt = Instant.now();
    }


    public static Document createDraft(
            Company company,
            Employee writer,
            FormTemplate template,
            String title,
            Document beforeDocument
    ) {
        return Document.builder()
                .company(company)
                .templateGroup(template.getTemplateGroup())
                .templateVersion(template.getVersion())
                .writer(writer)
                .title(title)
                .status(DocumentStatus.DRAFT)
                .beforeDocument(beforeDocument)
                .build();
    }


    public static Document reviseDraft(Document beforeDocument) {
        return Document.builder()
                .company(beforeDocument.getCompany())
                .templateGroup(beforeDocument.getTemplateGroup())
                .templateVersion(beforeDocument.getTemplateVersion())
                .writer(beforeDocument.getWriter())
                .title(beforeDocument.getTitle()+"(재기안)")
                .status(DocumentStatus.DRAFT)
                .beforeDocument(beforeDocument)
                .build();
    }

    public void reject() {
        this.status = DocumentStatus.REJECTED;
    }

    public void approve() {
        this.status = DocumentStatus.APPROVED;
    }

}
