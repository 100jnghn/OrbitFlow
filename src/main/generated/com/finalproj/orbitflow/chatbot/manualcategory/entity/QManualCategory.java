package com.finalproj.orbitflow.chatbot.manualcategory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QManualCategory is a Querydsl query type for ManualCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QManualCategory extends EntityPathBase<ManualCategory> {

    private static final long serialVersionUID = -290327838L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QManualCategory manualCategory = new QManualCategory("manualCategory");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final StringPath categoryName = createString("categoryName");

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QManualCategory(String variable) {
        this(ManualCategory.class, forVariable(variable), INITS);
    }

    public QManualCategory(Path<? extends ManualCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QManualCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QManualCategory(PathMetadata metadata, PathInits inits) {
        this(ManualCategory.class, metadata, inits);
    }

    public QManualCategory(Class<? extends ManualCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
    }

}

