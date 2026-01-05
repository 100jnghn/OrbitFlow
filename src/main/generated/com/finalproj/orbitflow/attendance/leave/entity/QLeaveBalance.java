package com.finalproj.orbitflow.attendance.leave.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLeaveBalance is a Querydsl query type for LeaveBalance
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLeaveBalance extends EntityPathBase<LeaveBalance> {

    private static final long serialVersionUID = -1225053966L;

    public static final QLeaveBalance leaveBalance = new QLeaveBalance("leaveBalance");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> employeeId = createNumber("employeeId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final NumberPath<java.math.BigDecimal> remainingDays = createNumber("remainingDays", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> totalGranted = createNumber("totalGranted", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public QLeaveBalance(String variable) {
        super(LeaveBalance.class, forVariable(variable));
    }

    public QLeaveBalance(Path<? extends LeaveBalance> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLeaveBalance(PathMetadata metadata) {
        super(LeaveBalance.class, metadata);
    }

}

