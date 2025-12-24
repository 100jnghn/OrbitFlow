package com.finalproj.orbitflow.chatbot.manualCategory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QManualCategory is a Querydsl query type for ManualCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QManualCategory extends EntityPathBase<ManualCategory> {

    private static final long serialVersionUID = -652443902L;

    public static final QManualCategory manualCategory = new QManualCategory("manualCategory");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final StringPath categoryName = createString("categoryName");

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

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
        super(ManualCategory.class, forVariable(variable));
    }

    public QManualCategory(Path<? extends ManualCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QManualCategory(PathMetadata metadata) {
        super(ManualCategory.class, metadata);
    }

}

