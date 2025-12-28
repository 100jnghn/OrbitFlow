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
import com.finalproj.orbitflow.hr.organization.entity.QOrganization;
import com.finalproj.orbitflow.hr.positionCategory.entity.QPositionCategory;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

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

        QEmployee currentApprover = new QEmployee("currentApprover");
        QOrganization currentOrg = new QOrganization("currentOrg");
        QPositionCategory currentPosition = new QPositionCategory("currentPosition");

        BooleanExpression whereCondition =
                document.company.id.eq(companyId)
                        .and(document.writer.id.eq(employeeId))
                        .and(documentStatusEq(reqDto.getDocumentStatus()))
                        .and(keywordEq(reqDto))
                        .and(createdAtBetween(reqDto));

        var baseQuery =
                queryFactory
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
                        .leftJoin(approvalLine.approver, currentApprover)
                        .leftJoin(currentApprover.organization, currentOrg)
                        .leftJoin(currentApprover.positionCategory, currentPosition)
                        .where(whereCondition);

        List<DocumentListResDto> content =
                baseQuery
                        .select(Projections.constructor(
                                DocumentListResDto.class,
                                document.id,
                                document.title,
                                templateGroup.name,
                                document.templateVersion,
                                document.createdAt,
                                document.status,

                                currentOrg.name,
                                currentPosition.name,
                                currentApprover.name,

                                approvalLine.orderNo
                        ))
                        .orderBy(document.createdAt.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        Long total =
                baseQuery
                        .select(document.count())
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

        QEmployee currentApprover = new QEmployee("currentApprover");
        QOrganization currentOrg = new QOrganization("currentOrg");
        QPositionCategory currentPosition = new QPositionCategory("currentPosition");

        BooleanExpression whereCondition =
                document.company.id.eq(companyId)
                        .and(myLine.approver.id.eq(employeeId))
                        .and(
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
                                        )
                        )
                        .and(approvalStatusEq(reqDto.getApprovalStatus()))
                        .and(keywordContains(reqDto.getKeyword()));

        var baseQuery =
                queryFactory
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
                        .leftJoin(currentLine.approver, currentApprover)
                        .leftJoin(currentApprover.organization, currentOrg)
                        .leftJoin(currentApprover.positionCategory, currentPosition)
                        .where(whereCondition);

        List<DocumentMyApprovalListResDto> content =
                baseQuery
                        .select(Projections.constructor(
                                DocumentMyApprovalListResDto.class,
                                document.id,
                                document.title,
                                templateGroup.name,
                                document.writer.name,
                                document.createdAt,
                                myLine.decidedAt,

                                // ⭐ 현재 결재자 정보
                                currentOrg.name,
                                currentPosition.name,
                                currentApprover.name,

                                // ⭐ 내 결재 상태
                                myLine.status
                        ))
                        .orderBy(document.createdAt.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        Long total =
                baseQuery
                        .select(document.count())
                        .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // ======================================================
    // 공통 where 조건 메서드
    // ======================================================
    private BooleanExpression documentStatusEq(DocumentStatus status) {
        return status != null ? document.status.eq(status) : null;
    }

    /**
     * 결재 상태 필터
     * - 결재함 기준에서는 myLine.status 의미
     */
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

    // ======================================================
    // 날짜 조건 (Instant 기반)
    // ======================================================
    private BooleanExpression createdAtBetween(DocumentListReqDto reqDto) {
        LocalDate start = reqDto.getStartDate();
        LocalDate end = reqDto.getEndDate();

        if (start == null && end == null) {
            return null;
        }

        if (start != null && end != null) {
            Instant startInstant =
                    start.atStartOfDay(KST).toInstant();

            Instant endInstant =
                    end.plusDays(1).atStartOfDay(KST).toInstant().minusNanos(1);

            return document.createdAt.between(startInstant, endInstant);
        }

        if (start != null) {
            return document.createdAt.goe(
                    start.atStartOfDay(KST).toInstant()
            );
        }

        return document.createdAt.loe(
                end.plusDays(1).atStartOfDay(KST).toInstant().minusNanos(1)
        );
    }
}
