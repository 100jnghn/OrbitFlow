package com.finalproj.orbitflow.global.analytics.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCompanyDailySnapshot is a Querydsl query type for CompanyDailySnapshot
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompanyDailySnapshot extends EntityPathBase<CompanyDailySnapshot> {

    private static final long serialVersionUID = 1253119318L;

    public static final QCompanyDailySnapshot companyDailySnapshot = new QCompanyDailySnapshot("companyDailySnapshot");

    public final StringPath activeYn = createString("activeYn");

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    public final NumberPath<Integer> employeeCount = createNumber("employeeCount", Integer.class);

    public final NumberPath<Long> fileBytes = createNumber("fileBytes", Long.class);

    public final NumberPath<Integer> fileCount = createNumber("fileCount", Integer.class);

    public final DatePath<java.time.LocalDate> snapshotDate = createDate("snapshotDate", java.time.LocalDate.class);

    public QCompanyDailySnapshot(String variable) {
        super(CompanyDailySnapshot.class, forVariable(variable));
    }

    public QCompanyDailySnapshot(Path<? extends CompanyDailySnapshot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCompanyDailySnapshot(PathMetadata metadata) {
        super(CompanyDailySnapshot.class, metadata);
    }

}

