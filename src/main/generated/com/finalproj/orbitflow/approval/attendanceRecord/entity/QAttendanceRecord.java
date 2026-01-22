package com.finalproj.orbitflow.approval.attendance.record.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAttendanceRecord is a Querydsl query type for AttendanceRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAttendanceRecord extends EntityPathBase<AttendanceRecord> {

    private static final long serialVersionUID = -1446246114L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAttendanceRecord attendanceRecord = new QAttendanceRecord("record");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> approvedAt = createDateTime("approvedAt", java.time.LocalDateTime.class);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<java.math.BigDecimal> days = createNumber("days", java.math.BigDecimal.class);

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee employee;

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.finalproj.orbitflow.attendance.leave.entity.QLeaveType leaveType;

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath reason = createString("reason");

    public final com.finalproj.orbitflow.approval.document.entity.QDocument sourceDocument;

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final EnumPath<com.finalproj.orbitflow.approval.document.enums.DocumentStatus> status = createEnum("status", com.finalproj.orbitflow.approval.document.enums.DocumentStatus.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QAttendanceRecord(String variable) {
        this(AttendanceRecord.class, forVariable(variable), INITS);
    }

    public QAttendanceRecord(Path<? extends AttendanceRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAttendanceRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAttendanceRecord(PathMetadata metadata, PathInits inits) {
        this(AttendanceRecord.class, metadata, inits);
    }

    public QAttendanceRecord(Class<? extends AttendanceRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.employee = inits.isInitialized("employee") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("employee"), inits.get("employee")) : null;
        this.leaveType = inits.isInitialized("leaveType") ? new com.finalproj.orbitflow.attendance.leave.entity.QLeaveType(forProperty("leaveType")) : null;
        this.sourceDocument = inits.isInitialized("sourceDocument") ? new com.finalproj.orbitflow.approval.document.entity.QDocument(forProperty("sourceDocument"), inits.get("sourceDocument")) : null;
    }

}

