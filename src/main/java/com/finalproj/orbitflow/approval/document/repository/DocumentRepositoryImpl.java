package com.finalproj.orbitflow.approval.document.repository;

import com.finalproj.orbitflow.approval.approvalLine.entity.QApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.document.dto.DocumentListReqDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentListResDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentMyApprovalListResDto;
import com.finalproj.orbitflow.approval.document.entity.QDocument;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.formTemplateGroup.entity.QFormTemplateGroup;
import com.finalproj.orbitflow.hr.employee.entity.QEmployee;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentRepositoryImpl
 * @since : 25. 12. 22. 월요일
 **/

@Repository
@RequiredArgsConstructor
public class DocumentRepositoryImpl implements DocumentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QDocument document = QDocument.document;
    private static final QFormTemplateGroup templateGroup = QFormTemplateGroup.formTemplateGroup;
    private static final QApprovalLine approvalLine = QApprovalLine.approvalLine;

    // ======================================================
    // 1. 내가 기안한 문서 조회 (기안함)
    // ======================================================
    @Override
    public Page<DocumentListResDto> findMyWrittenDocuments(
            Long companyId,
            Long employeeId,
            DocumentListReqDto reqDto,
            Pageable pageable
    ) {
        QApprovalLine approvalLineSub = new QApprovalLine("approvalLineSub");
        QEmployee approver = new QEmployee("approver");

        List<DocumentListResDto> content =
                queryFactory
                        .select(Projections.constructor(
                                DocumentListResDto.class,
                                document.title,
                                templateGroup.name,
                                document.templateVersion,
                                document.createdAt,
                                document.status,
                                approver.name,
                                approvalLine.orderNo
                        ))
                        .from(document)
                        .join(document.templateGroup, templateGroup)
                        .leftJoin(approvalLine).on(
                                approvalLine.document.eq(document),
                                approvalLine.status.eq(ApprovalStatus.WAITING),
                                approvalLine.orderNo.eq(
                                        JPAExpressions
                                                .select(approvalLineSub.orderNo.min())
                                                .from(approvalLineSub)
                                                .where(
                                                        approvalLineSub.document.eq(document),
                                                        approvalLineSub.status.eq(ApprovalStatus.WAITING)
                                                )
                                )
                        )
                        .leftJoin(approvalLine.approver, approver)
                        .where(
                                document.company.id.eq(companyId),
                                document.writer.id.eq(employeeId),
                                documentStatusEq(reqDto.getDocumentStatus()),
                                keywordEq(reqDto),
                                createdAtBetween(reqDto)
                        )
                        .orderBy(document.createdAt.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        Long total =
                queryFactory
                        .select(document.count())
                        .from(document)
                        .where(
                                document.company.id.eq(companyId),
                                document.writer.id.eq(employeeId),
                                documentStatusEq(reqDto.getDocumentStatus()),
                                keywordEq(reqDto),
                                createdAtBetween(reqDto)
                        )
                        .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // ======================================================
    // 2. 내가 결재자인 문서 조회 (결재함)
    // ======================================================
    @Override
    public Page<DocumentMyApprovalListResDto> findMyApprovalDocuments(
            Long companyId,
            Long employeeId,
            DocumentListReqDto reqDto,
            Pageable pageable
    ) {

        QApprovalLine myLine = QApprovalLine.approvalLine;
        QApprovalLine currentLine = new QApprovalLine("currentLine");
        QApprovalLine currentLineSub = new QApprovalLine("currentLineSub");

        List<DocumentMyApprovalListResDto> content =
                queryFactory
                        .select(Projections.constructor(
                                DocumentMyApprovalListResDto.class,
                                document.id,
                                document.title,
                                templateGroup.name,
                                document.writer.name,
                                document.createdAt,
                                myLine.decidedAt,
                                currentLine.approver.name,
                                myLine.status
                        ))
                        .from(myLine)
                        .join(myLine.document, document)
                        .join(document.templateGroup, templateGroup)
                        .join(document.writer)
                        .leftJoin(currentLine).on(
                                currentLine.document.eq(document),
                                currentLine.status.eq(ApprovalStatus.WAITING),
                                currentLine.orderNo.eq(
                                        JPAExpressions
                                                .select(currentLineSub.orderNo.min())
                                                .from(currentLineSub)
                                                .where(
                                                        currentLineSub.document.eq(document),
                                                        currentLineSub.status.eq(ApprovalStatus.WAITING)
                                                )
                                )
                        )
                        .where(
                                document.company.id.eq(companyId),
                                myLine.approver.id.eq(employeeId),

                                // 내가 처리했거나, 현재 내 차례인 문서
                                myLine.status.in(ApprovalStatus.APPROVED, ApprovalStatus.REJECTED)
                                        .or(
                                                myLine.status.eq(ApprovalStatus.WAITING)
                                                        .and(
                                                                myLine.orderNo.eq(
                                                                        JPAExpressions
                                                                                .select(currentLineSub.orderNo.min())
                                                                                .from(currentLineSub)
                                                                                .where(
                                                                                        currentLineSub.document.eq(document),
                                                                                        currentLineSub.status.eq(ApprovalStatus.WAITING)
                                                                                )
                                                                )
                                                        )
                                        ),

                                approvalStatusEq(reqDto.getApprovalStatus()),
                                keywordContains(reqDto.getKeyword())
                        )
                        .orderBy(document.createdAt.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        Long total =
                queryFactory
                        .select(document.count())
                        .from(myLine)
                        .join(myLine.document, document)
                        .where(
                                document.company.id.eq(companyId),
                                myLine.approver.id.eq(employeeId),

                                myLine.status.in(ApprovalStatus.APPROVED, ApprovalStatus.REJECTED)
                                        .or(
                                                myLine.status.eq(ApprovalStatus.WAITING)
                                                        .and(
                                                                myLine.orderNo.eq(
                                                                        JPAExpressions
                                                                                .select(currentLineSub.orderNo.min())
                                                                                .from(currentLineSub)
                                                                                .where(
                                                                                        currentLineSub.document.eq(document),
                                                                                        currentLineSub.status.eq(ApprovalStatus.WAITING)
                                                                                )
                                                                )
                                                        )
                                        ),

                                approvalStatusEq(reqDto.getApprovalStatus()),
                                keywordContains(reqDto.getKeyword())
                        )
                        .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // ======================================================
    // 공통 where 조건 메서드
    // ======================================================
    private BooleanExpression documentStatusEq(DocumentStatus status) {
        return status != null ? document.status.eq(status) : null;
    }

    private BooleanExpression approvalStatusEq(ApprovalStatus status) {
        return status != null ? approvalLine.status.eq(status) : null;
    }

    private BooleanExpression keywordEq(DocumentListReqDto reqDto) {
        if (!StringUtils.hasText(reqDto.getKeyword()) || reqDto.getSearchType() == null) {
            return null;
        }

        return switch (reqDto.getSearchType()) {
            case TITLE -> document.title.contains(reqDto.getKeyword());
            case GROUP_NAME -> templateGroup.name.contains(reqDto.getKeyword());
        };
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
                ? document.title.containsIgnoreCase(keyword)
                : null;
    }

    private BooleanExpression createdAtBetween(DocumentListReqDto reqDto) {
        // (생략: 네가 이미 작성한 로직 그대로)
        return null;
    }
}
