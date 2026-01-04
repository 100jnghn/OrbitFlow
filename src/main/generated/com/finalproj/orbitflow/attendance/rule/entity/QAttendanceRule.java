package com.finalproj.orbitflow.attendance.rule.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAttendanceRule is a Querydsl query type for AttendanceRule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAttendanceRule extends EntityPathBase<AttendanceRule> {

    private static final long serialVersionUID = 148269701L;

    public static final QAttendanceRule attendanceRule = new QAttendanceRule("attendanceRule");

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> defaultBreakMinutes = createNumber("defaultBreakMinutes", Integer.class);

    public final TimePath<java.time.LocalTime> defaultEndTime = createTime("defaultEndTime", java.time.LocalTime.class);

    public final TimePath<java.time.LocalTime> defaultStartTime = createTime("defaultStartTime", java.time.LocalTime.class);

    public final NumberPath<Integer> earlyLeaveThresholdMin = createNumber("earlyLeaveThresholdMin", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDefault = createBoolean("isDefault");

    public final NumberPath<Integer> lateThresholdMin = createNumber("lateThresholdMin", Integer.class);

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QAttendanceRule(String variable) {
        super(AttendanceRule.class, forVariable(variable));
    }

    public QAttendanceRule(Path<? extends AttendanceRule> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAttendanceRule(PathMetadata metadata) {
        super(AttendanceRule.class, metadata);
    }

}

