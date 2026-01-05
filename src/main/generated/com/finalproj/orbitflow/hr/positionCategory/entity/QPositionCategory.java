package com.finalproj.orbitflow.hr.positionCategory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPositionCategory is a Querydsl query type for PositionCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPositionCategory extends EntityPathBase<PositionCategory> {

    private static final long serialVersionUID = 836739601L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPositionCategory positionCategory = new QPositionCategory("positionCategory");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final BooleanPath isHead = createBoolean("isHead");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath name = createString("name");

    public final NumberPath<Integer> orderIndex = createNumber("orderIndex", Integer.class);

    public final com.finalproj.orbitflow.hr.orgCategory.entity.QOrgCategory orgCategory;

    public final QPositionCategory parent;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QPositionCategory(String variable) {
        this(PositionCategory.class, forVariable(variable), INITS);
    }

    public QPositionCategory(Path<? extends PositionCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPositionCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPositionCategory(PathMetadata metadata, PathInits inits) {
        this(PositionCategory.class, metadata, inits);
    }

    public QPositionCategory(Class<? extends PositionCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.orgCategory = inits.isInitialized("orgCategory") ? new com.finalproj.orbitflow.hr.orgCategory.entity.QOrgCategory(forProperty("orgCategory")) : null;
        this.parent = inits.isInitialized("parent") ? new QPositionCategory(forProperty("parent"), inits.get("parent")) : null;
    }

}

