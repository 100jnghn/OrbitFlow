package com.finalproj.orbitflow.approval.documentAISummary.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : DocumentAISummary
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.documentAISummary.enums.AiStatus;
import com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryType;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(
        name = "document_ai_summary",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_doc_ai_summary",
                        columnNames = {"document_id", "summary_type"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAISummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_type", nullable = false, length = 20)
    private SummaryType summaryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AiStatus status;

    @Column(name = "prompt", columnDefinition = "text")
    private String prompt;


    @Column(columnDefinition = "text")
    private String content;

    @Column(length = 50)
    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "before_document_id")
    private Document beforeDocument;


    /* 상태 변경용 메서드 */
    public void markCompleted(String content) {
        this.content = content;
        this.status = AiStatus.COMPLETED;
    }


    public void markFailed() {
        this.status = AiStatus.FAILED;
    }
}
