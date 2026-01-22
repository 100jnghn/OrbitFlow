package com.finalproj.orbitflow.approval.document.repository;

import com.finalproj.orbitflow.approval.document.dto.DocumentListReqDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentListResDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentMyApprovalListResDto;
import com.finalproj.orbitflow.approval.document.dto.ReferenceSearchResDto;
import com.finalproj.orbitflow.approval.document.entity.QDocument;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.file.entity.QDocumentFile;
import com.finalproj.orbitflow.approval.document.file.enums.ReferenceType;
import com.finalproj.orbitflow.approval.form.template.group.entity.QFormTemplateGroup;
import com.finalproj.orbitflow.approval.line.entity.QApprovalLine;
import com.finalproj.orbitflow.approval.line.enums.ApprovalStatus;
import com.finalproj.orbitflow.hr.employee.entity.QEmployee;
import com.finalproj.orbitflow.hr.organization.entity.QOrganization;
import com.finalproj.orbitflow.hr.positionCategory.entity.QPositionCategory;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
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

        QDocument revisionDoc = new QDocument("revisionDoc");

    /* ===============================
       WHERE 조건
    =============================== */
        BooleanExpression whereCondition =
                document.company.id.eq(companyId)
                        .and(document.writer.id.eq(employeeId))
                        .and(documentStatusEq(reqDto.getDocumentStatus()))
                        .and(keywordCondition(reqDto))
                        .and(createdAtBetween(reqDto));

    /* ===============================
       BASE QUERY
    =============================== */
        var baseQuery =
                queryFactory
                        .from(document)
                        .join(document.templateGroup, templateGroup)

                        // 현재 결재 단계 (IN_PROGRESS 중 최소 orderNo)
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

    /* ===============================
       CONTENT QUERY
    =============================== */
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

                                // 현재 결재 단계
                                approvalLine.orderNo,

                                // 전체 결재자 수
                                JPAExpressions
                                        .select(approvalLineSub.count())
                                        .from(approvalLineSub)
                                        .where(approvalLineSub.document.eq(document)),

                                // 현재 결재자 표시 정보
                                currentOrg.name,
                                currentPosition.name,
                                currentApprover.name,

                                // 재기안 여부
                                JPAExpressions
                                        .selectOne()
                                        .from(revisionDoc)
                                        .where(revisionDoc.beforeDocument.id.eq(document.id))
                                        .exists()
                        ))
                        .orderBy(
                                document.updatedAt.desc(),
                                document.createdAt.desc()
                        )
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

    /* ===============================
       COUNT QUERY
    =============================== */
        Long total =
                queryFactory
                        .select(document.id.countDistinct())
                        .from(document)
                        .where(whereCondition)
                        .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total == null ? 0 : total
        );
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

        BooleanExpression visibleMyLine =
                myLine.status.in(
                        ApprovalStatus.WAITING,
                        ApprovalStatus.IN_PROGRESS,
                        ApprovalStatus.APPROVED,
                        ApprovalStatus.REJECTED
                ).or(
                        myLine.status.eq(ApprovalStatus.CANCELLED)
                                .and(document.status.eq(DocumentStatus.REJECTED))
                );

        BooleanExpression whereCondition =
                document.company.id.eq(companyId)
                        .and(myLine.approver.id.eq(employeeId))
                        .and(visibleMyLine)
                        .and(document.status.ne(DocumentStatus.DRAFT))
                        .and(keywordCondition(reqDto))
                        .and(createdAtBetween(reqDto))
                        .and(documentStatusEq(reqDto.getDocumentStatus()))
                        .and(myApprovalStatusEq(reqDto.getApprovalStatus(), myLine));

    /* ===============================
       1️⃣ 정렬 우선순위 (핵심)
       IN_PROGRESS → WAITING → 나머지
    =============================== */

        NumberExpression<Integer> sortPriority =
                new CaseBuilder()
                        .when(myLine.status.eq(ApprovalStatus.IN_PROGRESS)).then(0)
                        .when(myLine.status.eq(ApprovalStatus.WAITING)).then(1)
                        .otherwise(2);

    /* ===============================
       2️⃣ WAITING 전용 remaining (정렬용)
    =============================== */

        NumberExpression<Integer> remainingForWaiting =
                new CaseBuilder()
                        .when(
                                myLine.status.eq(ApprovalStatus.WAITING)
                                        .and(currentLine.orderNo.isNotNull())
                        )
                        .then(myLine.orderNo.subtract(currentLine.orderNo))
                        .otherwise((Integer) null);

    /* ===============================
       3️⃣ WAITING 전용 remaining (표시용)
       → "대기중 (3명)"
    =============================== */

        NumberExpression<Integer> remainingForDisplay =
                new CaseBuilder()
                        .when(myLine.status.eq(ApprovalStatus.WAITING))
                        .then(myLine.orderNo.subtract(currentLine.orderNo))
                        .otherwise(0);

    /* ===============================
       문서 상태 (화면 표시용)
    =============================== */

        StringExpression documentDisplayStatus =
                new CaseBuilder()
                        .when(document.status.eq(DocumentStatus.IN_PROGRESS))
                        .then("진행중")
                        .when(document.status.eq(DocumentStatus.APPROVED))
                        .then("승인 완료")
                        .when(document.status.eq(DocumentStatus.REJECTED))
                        .then("반려 종료")
                        .otherwise("기타");

    /* ===============================
       BASE QUERY
    =============================== */

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
       CONTENT QUERY
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

                                documentDisplayStatus,

                                // 표시 조직
                                new CaseBuilder()
                                        .when(document.status.in(
                                                DocumentStatus.APPROVED,
                                                DocumentStatus.REJECTED
                                        ))
                                        .then(finalOrg.name)
                                        .otherwise(currentOrg.name),

                                // 표시 직책
                                new CaseBuilder()
                                        .when(document.status.in(
                                                DocumentStatus.APPROVED,
                                                DocumentStatus.REJECTED
                                        ))
                                        .then(finalPosition.name)
                                        .otherwise(currentPosition.name),

                                // 표시 이름
                                new CaseBuilder()
                                        .when(document.status.in(
                                                DocumentStatus.APPROVED,
                                                DocumentStatus.REJECTED
                                        ))
                                        .then(finalApprover.name)
                                        .otherwise(currentApprover.name),

                                // 내 결재 상태
                                myLine.status,

                                // ⭐ 대기중 표시용 remaining
                                remainingForDisplay
                        ))
                        .orderBy(
                                sortPriority.asc(),
                                remainingForWaiting.asc().nullsLast(),
                                myLine.decidedAt.desc().nullsLast(),
                                document.createdAt.desc()
                        )
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

    /* ===============================
       COUNT QUERY
    =============================== */

        Long total =
                queryFactory
                        .select(document.id.countDistinct())
                        .from(myLine)
                        .join(myLine.document, document)
                        .where(whereCondition)
                        .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total == null ? 0 : total
        );
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
        if (status == null) return null;

        return switch (status) {
            case CANCELLED -> myLine.status.eq(ApprovalStatus.CANCELLED)
                    .and(document.status.eq(DocumentStatus.REJECTED));

            default -> myLine.status.eq(status);
        };
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

    @Override
    public List<ReferenceSearchResDto> searchReference(
            Long employeeId,
            Long companyId,
            String keyword,
            int limit
    ) {
        QDocument d = QDocument.document;
        QDocumentFile df = QDocumentFile.documentFile;
        QApprovalLine al = QApprovalLine.approvalLine;
        QEmployee w = QEmployee.employee;

        return queryFactory
                .select(Projections.constructor(
                        ReferenceSearchResDto.class,
                        d.id,
                        d.title,
                        d.updatedAt,      // 승인 시점 없으니 이걸 사용
                        w.name,
                        df.id
                ))
                .from(d)
                .join(df).on(
                        df.document.eq(d),
                        df.referenceType.eq(ReferenceType.DOCUMENT),
                        df.referenceUrl.isNull()
                )
                .join(d.writer, w)
                .leftJoin(al).on(
                        al.document.eq(d),
                        al.approver.id.eq(employeeId)
                )
                .where(
                        d.company.id.eq(companyId),
                        d.status.eq(DocumentStatus.APPROVED),
                        d.isDeleted.isFalse(),
                        d.title.contains(keyword),
                        accessCondition(d, al, employeeId)
                )
                .orderBy(
                        similarityOrder(d.title, keyword),
                        d.updatedAt.desc()
                )
                .limit(limit)
                .fetch();
    }

    private BooleanExpression accessCondition(
            QDocument d,
            QApprovalLine al,
            Long employeeId
    ) {
        return d.writer.id.eq(employeeId)
                .or(al.approver.id.eq(employeeId));
    }

    private OrderSpecifier<Integer> similarityOrder(
            StringPath title,
            String keyword
    ) {
        return new CaseBuilder()
                .when(title.startsWithIgnoreCase(keyword)).then(0)
                .when(title.containsIgnoreCase(keyword)).then(1)
                .otherwise(2)
                .asc();
    }
}
