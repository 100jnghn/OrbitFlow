package com.finalproj.orbitflow.leave.grantHistory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGrantHistory is a Querydsl query type for GrantHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGrantHistory extends EntityPathBase<GrantHistory> {

    private static final long serialVersionUID = -633958686L;

    public static final QGrantHistory grantHistory = new QGrantHistory("grantHistory");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> employeeId = createNumber("employeeId", Long.class);

    public final DatePath<java.time.LocalDate> expirationDate = createDate("expirationDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> grantDate = createDate("grantDate", java.time.LocalDate.class);

    public final NumberPath<java.math.BigDecimal> grantedDays = createNumber("grantedDays", java.math.BigDecimal.class);

    public final StringPath grantType = createString("grantType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isExpired = createBoolean("isExpired");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QGrantHistory(String variable) {
        super(GrantHistory.class, forVariable(variable));
    }

    public QGrantHistory(Path<? extends GrantHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGrantHistory(PathMetadata metadata) {
        super(GrantHistory.class, metadata);
    }

}

