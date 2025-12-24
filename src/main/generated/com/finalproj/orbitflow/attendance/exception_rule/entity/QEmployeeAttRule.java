package com.finalproj.orbitflow.attendance.exception_rule.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmployeeAttRule is a Querydsl query type for EmployeeAttRule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmployeeAttRule extends EntityPathBase<EmployeeAttRule> {

    private static final long serialVersionUID = -1328090497L;

    public static final QEmployeeAttRule employeeAttRule = new QEmployeeAttRule("employeeAttRule");

    public final DateTimePath<java.time.LocalDateTime> appliedAt = createDateTime("appliedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> breakMinutes = createNumber("breakMinutes", Integer.class);

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    public final NumberPath<Long> employeeId = createNumber("employeeId", Long.class);

    public final TimePath<java.time.LocalTime> endTime = createTime("endTime", java.time.LocalTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath reason = createString("reason");

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public final DatePath<java.time.LocalDate> validFrom = createDate("validFrom", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> validTo = createDate("validTo", java.time.LocalDate.class);

    public QEmployeeAttRule(String variable) {
        super(EmployeeAttRule.class, forVariable(variable));
    }

    public QEmployeeAttRule(Path<? extends EmployeeAttRule> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmployeeAttRule(PathMetadata metadata) {
        super(EmployeeAttRule.class, metadata);
    }

}

