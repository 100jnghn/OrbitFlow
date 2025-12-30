package com.finalproj.orbitflow.approval.formTemplateGroup.repository;

import com.finalproj.orbitflow.approval.formTemplate.entity.QFormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.formTemplateGroup.dto.FormTemplateGroupListResDto;
import com.finalproj.orbitflow.approval.formTemplateGroup.entity.QFormTemplateGroup;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateGroupRepositoryImpl
 * @since : 25. 12. 30. 화요일
 **/


@Repository
@RequiredArgsConstructor
public class FormTemplateGroupRepositoryImpl
        implements FormTemplateGroupRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FormTemplateGroupListResDto> findLatestGroupsWithActiveTemplate(
            Long companyId,
            String keyword
    ) {
        QFormTemplateGroup g = QFormTemplateGroup.formTemplateGroup;
        QFormTemplate t = QFormTemplate.formTemplate;

        BooleanExpression keywordCondition = hasKeyword(keyword)
                ? g.name.containsIgnoreCase(keyword.trim())
                : null;

        return queryFactory
                .select(Projections.constructor(
                        FormTemplateGroupListResDto.class,
                        g.id,
                        g.name,
                        g.description,
                        g.templateCategory.code,
                        g.baseRole,
                        g.active,

                        // ⭐ ACTIVE 문서 존재 여부
                        JPAExpressions
                                .selectOne()
                                .from(t)
                                .where(
                                        t.templateGroup.eq(g),
                                        t.status.eq(FormTemplateStatus.ACTIVE)
                                )
                                .exists()
                ))
                .from(g)
                .where(
                        g.company.id.eq(companyId),
                        keywordCondition
                )
                .orderBy(g.createdAt.desc())
                .limit(10)
                .fetch();
    }

    private boolean hasKeyword(String keyword) {
        return keyword != null && !keyword.trim().isEmpty();
    }
}
