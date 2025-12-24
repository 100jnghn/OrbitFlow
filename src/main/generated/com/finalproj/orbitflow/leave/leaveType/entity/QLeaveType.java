package com.finalproj.orbitflow.leave.leaveType.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLeaveType is a Querydsl query type for LeaveType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLeaveType extends EntityPathBase<LeaveType> {

    private static final long serialVersionUID = 341388664L;

    public static final QLeaveType leaveType = new QLeaveType("leaveType");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isCountable = createBoolean("isCountable");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath typeName = createString("typeName");

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QLeaveType(String variable) {
        super(LeaveType.class, forVariable(variable));
    }

    public QLeaveType(Path<? extends LeaveType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLeaveType(PathMetadata metadata) {
        super(LeaveType.class, metadata);
    }

}

