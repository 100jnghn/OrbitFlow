package com.finalproj.orbitflow.schedule.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QScheduleSummary is a Querydsl query type for ScheduleSummary
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScheduleSummary extends EntityPathBase<ScheduleSummary> {

    private static final long serialVersionUID = -456222115L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QScheduleSummary scheduleSummary = new QScheduleSummary("scheduleSummary");

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee employee;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath monthSummary = createString("monthSummary");

    public final StringPath weekSummary = createString("weekSummary");

    public QScheduleSummary(String variable) {
        this(ScheduleSummary.class, forVariable(variable), INITS);
    }

    public QScheduleSummary(Path<? extends ScheduleSummary> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QScheduleSummary(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QScheduleSummary(PathMetadata metadata, PathInits inits) {
        this(ScheduleSummary.class, metadata, inits);
    }

    public QScheduleSummary(Class<? extends ScheduleSummary> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.employee = inits.isInitialized("employee") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("employee"), inits.get("employee")) : null;
    }

}

