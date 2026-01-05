package com.finalproj.orbitflow.attendance.commute.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAttendance is a Querydsl query type for Attendance
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAttendance extends EntityPathBase<Attendance> {

    private static final long serialVersionUID = 2130067033L;

    public static final QAttendance attendance = new QAttendance("attendance");

    public final NumberPath<Long> appliedRuleId = createNumber("appliedRuleId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> commuteAt = createDateTime("commuteAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    public final StringPath correctionReason = createString("correctionReason");

    public final NumberPath<Long> employeeId = createNumber("employeeId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isCorrected = createBoolean("isCorrected");

    public final DateTimePath<java.time.LocalDateTime> leaveAt = createDateTime("leaveAt", java.time.LocalDateTime.class);

    public final EnumPath<com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus> status = createEnum("status", com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus.class);

    public final DatePath<java.time.LocalDate> workDate = createDate("workDate", java.time.LocalDate.class);

    public QAttendance(String variable) {
        super(Attendance.class, forVariable(variable));
    }

    public QAttendance(Path<? extends Attendance> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAttendance(PathMetadata metadata) {
        super(Attendance.class, metadata);
    }

}

