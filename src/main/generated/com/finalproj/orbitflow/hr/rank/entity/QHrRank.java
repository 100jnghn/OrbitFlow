package com.finalproj.orbitflow.hr.rank.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHrRank is a Querydsl query type for HrRank
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHrRank extends EntityPathBase<HrRank> {

    private static final long serialVersionUID = 1730694341L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHrRank hrRank = new QHrRank("hrRank");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath name = createString("name");

    public final NumberPath<Integer> orderIndex = createNumber("orderIndex", Integer.class);

    public final QHrRank parentHrRank;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QHrRank(String variable) {
        this(HrRank.class, forVariable(variable), INITS);
    }

    public QHrRank(Path<? extends HrRank> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHrRank(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHrRank(PathMetadata metadata, PathInits inits) {
        this(HrRank.class, metadata, inits);
    }

    public QHrRank(Class<? extends HrRank> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.parentHrRank = inits.isInitialized("parentHrRank") ? new QHrRank(forProperty("parentHrRank"), inits.get("parentHrRank")) : null;
    }

}

