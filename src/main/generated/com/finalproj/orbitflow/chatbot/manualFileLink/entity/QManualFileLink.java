package com.finalproj.orbitflow.chatbot.manualFileLink.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QManualFileLink is a Querydsl query type for ManualFileLink
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QManualFileLink extends EntityPathBase<ManualFileLink> {

    private static final long serialVersionUID = 644971058L;

    public static final QManualFileLink manualFileLink = new QManualFileLink("manualFileLink");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> fileId = createNumber("fileId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath status = createString("status");

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public final DateTimePath<java.time.LocalDateTime> vectorizedAt = createDateTime("vectorizedAt", java.time.LocalDateTime.class);

    public QManualFileLink(String variable) {
        super(ManualFileLink.class, forVariable(variable));
    }

    public QManualFileLink(Path<? extends ManualFileLink> path) {
        super(path.getType(), path.getMetadata());
    }

    public QManualFileLink(PathMetadata metadata) {
        super(ManualFileLink.class, metadata);
    }

}

