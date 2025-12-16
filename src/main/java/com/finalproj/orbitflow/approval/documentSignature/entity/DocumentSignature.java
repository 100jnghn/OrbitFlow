package com.finalproj.orbitflow.approval.documentSignature.entity;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentSignature
 * @since : 25. 12. 16. 화요일
 **/


@Entity
@Table(
        name = "document_signature",
        indexes = {
                @Index(name = "idx_doc_sig_document", columnList = "document_id"),
                @Index(name = "idx_doc_sig_employee", columnList = "employee_id")
        }
)
@Getter
@NoArgsConstructor
public class DocumentSignature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_signature_id")
    private Long id;

    /* =========================
       Company
       ========================= */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /* =========================
       Document (결재 문서)
       ========================= */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /* =========================
       Approver (결재자)
       ========================= */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /* =========================
       실제 찍힌 서명 이미지
       (EmployeeSignature와 분리)
       ========================= */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    /* =========================
       결재 단계 (1차, 2차 등)
       ========================= */
    @Column(name = "approval_order", nullable = false)
    private Integer approvalOrder;

    /* =========================
       생성 메서드
       ========================= */
    public static DocumentSignature create(
            Company company,
            Document document,
            Employee employee,
            File file,
            int approvalOrder
    ) {
        DocumentSignature ds = new DocumentSignature();
        ds.company = company;
        ds.document = document;
        ds.employee = employee;
        ds.file = file;
        ds.approvalOrder = approvalOrder;
        return ds;
    }
}