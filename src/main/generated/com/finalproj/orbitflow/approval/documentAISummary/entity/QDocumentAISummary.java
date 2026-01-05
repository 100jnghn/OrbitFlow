package com.finalproj.orbitflow.approval.documentAISummary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDocumentAISummary is a Querydsl query type for DocumentAISummary
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDocumentAISummary extends EntityPathBase<DocumentAISummary> {

    private static final long serialVersionUID = -751019536L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDocumentAISummary documentAISummary = new QDocumentAISummary("documentAISummary");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.approval.document.entity.QDocument beforeDocument;

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final com.finalproj.orbitflow.approval.document.entity.QDocument document;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath model = createString("model");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath prompt = createString("prompt");

    public final EnumPath<com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryStatus> status = createEnum("status", com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryStatus.class);

    public final EnumPath<com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryType> summaryType = createEnum("summaryType", com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryType.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QDocumentAISummary(String variable) {
        this(DocumentAISummary.class, forVariable(variable), INITS);
    }

    public QDocumentAISummary(Path<? extends DocumentAISummary> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDocumentAISummary(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDocumentAISummary(PathMetadata metadata, PathInits inits) {
        this(DocumentAISummary.class, metadata, inits);
    }

    public QDocumentAISummary(Class<? extends DocumentAISummary> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.beforeDocument = inits.isInitialized("beforeDocument") ? new com.finalproj.orbitflow.approval.document.entity.QDocument(forProperty("beforeDocument"), inits.get("beforeDocument")) : null;
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.document = inits.isInitialized("document") ? new com.finalproj.orbitflow.approval.document.entity.QDocument(forProperty("document"), inits.get("document")) : null;
    }

}

