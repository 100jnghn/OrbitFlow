
package com.finalproj.orbitflow.approval.document.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDocument is a Querydsl query type for Document
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDocument extends EntityPathBase<Document> {

    private static final long serialVersionUID = -1756276448L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDocument document = new QDocument("document");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final QDocument beforeDocument;

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final EnumPath<com.finalproj.orbitflow.approval.document.enums.DocumentStatus> status = createEnum("status", com.finalproj.orbitflow.approval.document.enums.DocumentStatus.class);

    public final DateTimePath<java.time.Instant> submittedAt = createDateTime("submittedAt", java.time.Instant.class);

    public final com.finalproj.orbitflow.approval.formTemplateGroup.entity.QFormTemplateGroup templateGroup;

    public final NumberPath<Integer> templateVersion = createNumber("templateVersion", Integer.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee writer;

    public QDocument(String variable) {
        this(Document.class, forVariable(variable), INITS);
    }

    public QDocument(Path<? extends Document> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDocument(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDocument(PathMetadata metadata, PathInits inits) {
        this(Document.class, metadata, inits);
    }

    public QDocument(Class<? extends Document> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.beforeDocument = inits.isInitialized("beforeDocument") ? new QDocument(forProperty("beforeDocument"), inits.get("beforeDocument")) : null;
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.templateGroup = inits.isInitialized("templateGroup") ? new com.finalproj.orbitflow.approval.formTemplateGroup.entity.QFormTemplateGroup(forProperty("templateGroup"), inits.get("templateGroup")) : null;
        this.writer = inits.isInitialized("writer") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("writer"), inits.get("writer")) : null;
    }

}
