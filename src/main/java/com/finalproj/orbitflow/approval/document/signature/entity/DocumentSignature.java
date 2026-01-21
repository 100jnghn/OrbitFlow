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
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentSignature
 * @since : 25. 12. 16. 화요일
 **/


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
