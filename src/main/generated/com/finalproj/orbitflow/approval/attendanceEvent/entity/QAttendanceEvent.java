package com.finalproj.orbitflow.approval.attendanceEvent.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAttendanceEvent is a Querydsl query type for AttendanceEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAttendanceEvent extends EntityPathBase<AttendanceEvent> {

    private static final long serialVersionUID = -1911027536L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAttendanceEvent attendanceEvent = new QAttendanceEvent("attendanceEvent");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final DatePath<java.time.LocalDate> actualEndDate = createDate("actualEndDate", java.time.LocalDate.class);

    public final EnumPath<com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole> baseRole = createEnum("baseRole", com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole.class);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee employee;

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final com.finalproj.orbitflow.approval.document.entity.QDocument sourceDocument;

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QAttendanceEvent(String variable) {
        this(AttendanceEvent.class, forVariable(variable), INITS);
    }

    public QAttendanceEvent(Path<? extends AttendanceEvent> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAttendanceEvent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAttendanceEvent(PathMetadata metadata, PathInits inits) {
        this(AttendanceEvent.class, metadata, inits);
    }

    public QAttendanceEvent(Class<? extends AttendanceEvent> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.employee = inits.isInitialized("employee") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("employee"), inits.get("employee")) : null;
        this.sourceDocument = inits.isInitialized("sourceDocument") ? new com.finalproj.orbitflow.approval.document.entity.QDocument(forProperty("sourceDocument"), inits.get("sourceDocument")) : null;
    }

}

