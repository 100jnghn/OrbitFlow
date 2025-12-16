package com.finalproj.orbitflow.approval.documentAISummary.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : DocumentAISummary
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryType;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_ai_summary")
@Getter
@NoArgsConstructor
public class DocumentAISummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    private SummaryType summaryType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "before_document_id")
    private Document beforeDocument;
}
