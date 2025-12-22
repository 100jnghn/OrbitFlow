package com.finalproj.orbitflow.approval.document.repository;

import com.finalproj.orbitflow.approval.approvalLine.entity.QApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.document.dto.DocumentListReqDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentListResDto;
import com.finalproj.orbitflow.approval.document.entity.QDocument;
import com.finalproj.orbitflow.approval.formTemplateGroup.entity.QFormTemplateGroup;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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

    @Override
    public Page<DocumentListResDto> findMyDocuments(
            Long companyId,
            Long employeeId,
            DocumentListReqDto reqDto,
            Pageable pageable
    ) {

        List<DocumentListResDto> content =
                queryFactory
                        .select(
                                Projections.constructor(
                                        DocumentListResDto.class,
                                        document.title,
                                        templateGroup.name,
                                        document.templateVersion,
                                        document.createdAt,
                                        document.status,
                                        approvalLine.approver.name,
                                        approvalLine.orderNo
                                )
                        )
                        .from(document)
                        .join(document.templateGroup, templateGroup)
                        .leftJoin(approvalLine)
                        .on(
                                approvalLine.document.eq(document),
                                approvalLine.status.eq(
                                        ApprovalStatus.IN_PROGRESS
                                )
                        )
                        .where(
                                document.company.id.eq(companyId),
                                document.writer.id.eq(employeeId),
                                statusEq(reqDto),
                                keywordEq(reqDto),
                                createdAtBetween(reqDto)
                        )
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .orderBy(document.createdAt.desc())
                        .fetch();

        Long total =
                queryFactory
                        .select(document.count())
                        .from(document)
                        .join(document.templateGroup, templateGroup)
                        .where(
                                document.company.id.eq(companyId),
                                document.writer.id.eq(employeeId),
                                statusEq(reqDto),
                                keywordEq(reqDto)
                        )
                        .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // =========================
    // where 조건 분리
    // =========================

    private BooleanExpression statusEq(DocumentListReqDto reqDto) {
        return reqDto.getStatus() != null
                ? document.status.eq(reqDto.getStatus())
                : null;
    }

    private BooleanExpression keywordEq(DocumentListReqDto reqDto) {
        if (reqDto.getKeyword() == null || reqDto.getKeyword().isBlank()) {
            return null;
        }

        return switch (reqDto.getSearchType()) {
            case TITLE -> document.title.contains(reqDto.getKeyword());
            case GROUP_NAME -> templateGroup.name.contains(reqDto.getKeyword());
        };
    }

    private BooleanExpression createdAtBetween(DocumentListReqDto reqDto) {
        if (reqDto.getStartDate() == null && reqDto.getEndDate() == null) {
            return null;
        }

        if (reqDto.getStartDate() != null && reqDto.getEndDate() != null) {
            return document.createdAt.between(
                    reqDto.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    reqDto.getEndDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            );
        }

        if (reqDto.getStartDate() != null) {
            return document.createdAt.goe(
                    reqDto.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            );
        }

        return document.createdAt.loe(
                reqDto.getEndDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        );
    }

}
