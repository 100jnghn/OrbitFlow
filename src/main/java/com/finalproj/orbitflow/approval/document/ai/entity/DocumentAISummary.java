package com.finalproj.orbitflow.approval.document.ai.entity;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.ai.enums.AiStatus;
import com.finalproj.orbitflow.approval.document.ai.enums.SummaryType;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결재 문서에 대한 AI 요약 및 변경 비교 결과를 저장하는 엔티티.
 * <p>
 * 하나의 결재 문서(Document)에 대해 요약(summary) 또는 변경 비교(diff) 유형별로
 * AI 처리 상태와 생성 결과를 관리한다.
 * <p>
 * AI 작업은 비동기 방식으로 수행되며,
 * 이 엔티티는 요청 시점의 프롬프트, 처리 상태(PROCESSING / COMPLETED / FAILED),
 * 그리고 최종 생성된 결과 내용을 영속화하는 역할을 한다.
 * <p>
 * (document_id, summary_type) 조합에 대해 유니크 제약을 두어,
 * 동일 문서에 동일 유형의 AI 결과가 중복 생성되지 않도록 한다.
 * <p>
 * 변경 비교(diff) 유형의 경우,
 * 이전 버전 문서를 beforeDocument로 함께 참조하여
 * 어떤 문서 기준으로 비교가 수행되었는지 추적할 수 있도록 설계되었다.
 * <p>
 * 이 엔티티는 AI 처리 흐름의 상태 관리와 결과 보관만을 책임하며,
 * 실제 AI 호출 및 프롬프트 생성 로직은 서비스 레이어에서 처리된다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentAISummary
 * @since : 25. 12. 15. 월요일
 **/

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


    public void markFailed(String failMessage) {
        this.status = AiStatus.FAILED;
        if(failMessage != null) {
            this.content = failMessage;
        }
    }
}
