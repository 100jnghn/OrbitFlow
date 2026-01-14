package com.finalproj.orbitflow.global.analytics.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCompanyDailyAiUsage is a Querydsl query type for CompanyDailyAiUsage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompanyDailyAiUsage extends EntityPathBase<CompanyDailyAiUsage> {

    private static final long serialVersionUID = -1125687161L;

    public static final QCompanyDailyAiUsage companyDailyAiUsage = new QCompanyDailyAiUsage("companyDailyAiUsage");

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    public final NumberPath<Integer> compareSummaryCnt = createNumber("compareSummaryCnt", Integer.class);

    public final NumberPath<Integer> docSummaryCnt = createNumber("docSummaryCnt", Integer.class);

    public final NumberPath<Integer> outlineGenCnt = createNumber("outlineGenCnt", Integer.class);

    public final NumberPath<Integer> scheduleSummaryCnt = createNumber("scheduleSummaryCnt", Integer.class);

    public final NumberPath<Integer> totalCnt = createNumber("totalCnt", Integer.class);

    public final DatePath<java.time.LocalDate> usageDate = createDate("usageDate", java.time.LocalDate.class);

    public QCompanyDailyAiUsage(String variable) {
        super(CompanyDailyAiUsage.class, forVariable(variable));
    }

    public QCompanyDailyAiUsage(Path<? extends CompanyDailyAiUsage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCompanyDailyAiUsage(PathMetadata metadata) {
        super(CompanyDailyAiUsage.class, metadata);
    }

}

