package com.finalproj.orbitflow.approval.documentFile.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : DocumentFile
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.documentFile.enums.ReferenceType;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_file")
@Getter
@NoArgsConstructor
public class DocumentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    private File file;

    @Enumerated(EnumType.STRING)
    private ReferenceType referenceType;

    private Long referenceTargetId;
    private String referenceUrl;
}