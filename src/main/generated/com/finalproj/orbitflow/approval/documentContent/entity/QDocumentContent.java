package com.finalproj.orbitflow.approval.document.content.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDocumentContent is a Querydsl query type for DocumentContent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDocumentContent extends EntityPathBase<DocumentContent> {

    private static final long serialVersionUID = 1769403920L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDocumentContent documentContent = new QDocumentContent("content");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final StringPath contentJson = createString("contentJson");

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final com.finalproj.orbitflow.approval.document.entity.QDocument document;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QDocumentContent(String variable) {
        this(DocumentContent.class, forVariable(variable), INITS);
    }

    public QDocumentContent(Path<? extends DocumentContent> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDocumentContent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDocumentContent(PathMetadata metadata, PathInits inits) {
        this(DocumentContent.class, metadata, inits);
    }

    public QDocumentContent(Class<? extends DocumentContent> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.document = inits.isInitialized("document") ? new com.finalproj.orbitflow.approval.document.entity.QDocument(forProperty("document"), inits.get("document")) : null;
    }

}

