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
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
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
                        .and(keywordCondition(reqDto))
                        .and(createdAtBetween(reqDto));

        var baseQuery =
                queryFactory
                        .from(document)
                        .join(document.templateGroup, templateGroup)
                        .leftJoin(approvalLine).on(
                                approvalLine.document.eq(document),
                                approvalLine.status.eq(ApprovalStatus.IN_PROGRESS),
                                approvalLine.orderNo.eq(
                                        JPAExpressions
                                                .select(approvalLineSub.orderNo.min())
                                                .from(approvalLineSub)
                                                .where(
                                                        approvalLineSub.document.eq(document),
                                                        approvalLineSub.status.eq(ApprovalStatus.IN_PROGRESS)
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

        // 현재 결재자
        QApprovalLine currentLine = new QApprovalLine("currentLine");
        QApprovalLine currentLineSub = new QApprovalLine("currentLineSub");

        // 최종 처리자
        QApprovalLine finalLine = new QApprovalLine("finalLine");
        QApprovalLine finalLineSub = new QApprovalLine("finalLineSub");

        QEmployee currentApprover = new QEmployee("currentApprover");
        QOrganization currentOrg = new QOrganization("currentOrg");
        QPositionCategory currentPosition = new QPositionCategory("currentPosition");

        QEmployee finalApprover = new QEmployee("finalApprover");
        QOrganization finalOrg = new QOrganization("finalOrg");
        QPositionCategory finalPosition = new QPositionCategory("finalPosition");

    /* ===============================
       WHERE 조건
    =============================== */
        BooleanExpression whereCondition =
                document.company.id.eq(companyId)
                        .and(myLine.approver.id.eq(employeeId))
                        .and(myLine.status.in(
                                ApprovalStatus.WAITING,
                                ApprovalStatus.IN_PROGRESS,
                                ApprovalStatus.APPROVED,
                                ApprovalStatus.REJECTED
                        ))
                        .and(document.status.ne(DocumentStatus.DRAFT))
                        .and(keywordCondition(reqDto))
                        .and(createdAtBetween(reqDto))
                        .and(documentStatusEq(reqDto.getDocumentStatus()))
                        .and(myApprovalStatusEq(reqDto.getApprovalStatus(), myLine));

    /* ===============================
       내 차례까지 남은 결재 인원 수
       =============================== */
        NumberExpression<Integer> remainingBeforeMyTurn =
                new CaseBuilder()
                        // 이미 내가 결재 완료
                        .when(myLine.status.in(
                                ApprovalStatus.APPROVED,
                                ApprovalStatus.REJECTED
                        ))
                        .then(0)

                        // 현재 결재자가 없는 경우 (완료 상태 등)
                        .when(currentLine.orderNo.isNull())
                        .then(0)

                        // 정상 계산
                        .otherwise(
                                myLine.orderNo.subtract(currentLine.orderNo)
                        );


        var baseQuery =
                queryFactory
                        .from(myLine)
                        .join(myLine.document, document)
                        .join(document.templateGroup, templateGroup)
                        .join(document.writer)

                        /* ===== 현재 결재자 ===== */
                        .leftJoin(currentLine).on(
                                currentLine.document.eq(document),
                                currentLine.status.eq(ApprovalStatus.IN_PROGRESS),
                                currentLine.orderNo.eq(
                                        JPAExpressions
                                                .select(currentLineSub.orderNo.min())
                                                .from(currentLineSub)
                                                .where(
                                                        currentLineSub.document.eq(document),
                                                        currentLineSub.status.eq(ApprovalStatus.IN_PROGRESS)
                                                )
                                )
                        )
                        .leftJoin(currentLine.approver, currentApprover)
                        .leftJoin(currentApprover.organization, currentOrg)
                        .leftJoin(currentApprover.positionCategory, currentPosition)

                        /* ===== 최종 처리자 ===== */
                        .leftJoin(finalLine).on(
                                finalLine.document.eq(document),
                                finalLine.status.in(
                                        ApprovalStatus.APPROVED,
                                        ApprovalStatus.REJECTED
                                ),
                                finalLine.orderNo.eq(
                                        JPAExpressions
                                                .select(finalLineSub.orderNo.max())
                                                .from(finalLineSub)
                                                .where(
                                                        finalLineSub.document.eq(document),
                                                        finalLineSub.status.in(
                                                                ApprovalStatus.APPROVED,
                                                                ApprovalStatus.REJECTED
                                                        )
                                                )
                                )
                        )
                        .leftJoin(finalLine.approver, finalApprover)
                        .leftJoin(finalApprover.organization, finalOrg)
                        .leftJoin(finalApprover.positionCategory, finalPosition)

                        .where(whereCondition);

    /* ===============================
       SELECT
    =============================== */
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

                                // 상태별 표시 담당자 (조직)
                                new CaseBuilder()
                                        .when(document.status.in(
                                                DocumentStatus.APPROVED,
                                                DocumentStatus.REJECTED
                                        ))
                                        .then(finalOrg.name)
                                        .otherwise(currentOrg.name),

                                // 상태별 표시 담당자 (직책)
                                new CaseBuilder()
                                        .when(document.status.in(
                                                DocumentStatus.APPROVED,
                                                DocumentStatus.REJECTED
                                        ))
                                        .then(finalPosition.name)
                                        .otherwise(currentPosition.name),

                                // 상태별 표시 담당자 (이름)
                                new CaseBuilder()
                                        .when(document.status.in(
                                                DocumentStatus.APPROVED,
                                                DocumentStatus.REJECTED
                                        ))
                                        .then(finalApprover.name)
                                        .otherwise(currentApprover.name),

                                // 내 결재 상태
                                myLine.status,

                                // 내 차례까지 남은 결재 인원 수
                                remainingBeforeMyTurn
                        ))
                        .orderBy(
                                // ⭐ UX 최적화: 내 차례 문서 우선
                                remainingBeforeMyTurn.asc().nullsLast(),
                                document.createdAt.desc()
                        )
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
    private BooleanExpression myApprovalStatusEq(
            ApprovalStatus status,
            QApprovalLine myLine
    ) {
        return status != null
                ? myLine.status.eq(status)
                : null;
    }

    private BooleanExpression keywordCondition(
            DocumentListReqDto reqDto
    ) {
        if (!StringUtils.hasText(reqDto.getKeyword())
                || reqDto.getSearchType() == null) {
            return null;
        }

        return switch (reqDto.getSearchType()) {
            case TITLE -> document.title.containsIgnoreCase(reqDto.getKeyword());

            case GROUP_NAME -> templateGroup.name.containsIgnoreCase(reqDto.getKeyword());
        };
    }


    // ======================================================
    // 날짜 조건 (Instant 기반)
    // ======================================================
    private BooleanExpression createdAtBetween(
            DocumentListReqDto reqDto
    ) {
        LocalDate start = reqDto.getStartDate();
        LocalDate end = reqDto.getEndDate();

        if (start == null && end == null) {
            return null;
        }

        if (start != null && end != null) {
            Instant startInstant =
                    start.atStartOfDay(KST).toInstant();

            Instant endInstant =
                    end.plusDays(1)
                            .atStartOfDay(KST)
                            .toInstant()
                            .minusNanos(1);

            return document.createdAt.between(startInstant, endInstant);
        }

        if (start != null) {
            return document.createdAt.goe(
                    start.atStartOfDay(KST).toInstant()
            );
        }

        return document.createdAt.loe(
                end.plusDays(1)
                        .atStartOfDay(KST)
                        .toInstant()
                        .minusNanos(1)
        );
    }
}
