package com.finalproj.orbitflow.approval.document.signature.entity;

import com.finalproj.orbitflow.approval.line.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 결재 문서의 특정 결재 단계에서 사용된 서명 정보를 저장하는 엔티티.
 * <p>
 * 하나의 결재 문서(Document)와 결재선(ApprovalLine)에 대해,
 * 실제 결재 시점에 사용된 서명 파일과 서명자를 기록한다.
 * <p>
 * 서명은 사원이 보유한 기본 서명을 그대로 참조하지 않고,
 * 결재 당시 사용된 서명 이미지를 스냅샷 형태로 저장한다.
 * 이를 통해 이후 문서 이력 조회나 감사 시점에도
 * 당시의 서명 상태를 그대로 확인할 수 있다.
 * <p>
 * (document_id, approval_line_id) 조합에 유니크 제약을 두어
 * 하나의 결재 단계에서 서명이 중복 생성되지 않도록 한다.
 * <p>
 * 이 엔티티는 결재 결과나 진행 상태를 관리하지 않으며,
 * 서명 이력 자체만을 기록하는 용도로 사용된다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentSignature
 * @since : 25. 12. 16. 화요일
 */


@Entity
@Table(
        name = "document_signature",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_doc_approval",
                        columnNames = {"document_id", "approval_line_id"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSignature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_line_id", nullable = false)
    private ApprovalLine approvalLine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signer_id", nullable = false)
    private Employee signer;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signature_file_id", nullable = false)
    private File signatureFile;
}
