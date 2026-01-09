package com.finalproj.orbitflow.approval.documentSignature.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDocumentSignature is a Querydsl query type for DocumentSignature
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDocumentSignature extends EntityPathBase<DocumentSignature> {

    private static final long serialVersionUID = 782430000L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDocumentSignature documentSignature = new QDocumentSignature("documentSignature");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.approval.approvalLine.entity.QApprovalLine approvalLine;

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final com.finalproj.orbitflow.approval.document.entity.QDocument document;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final com.finalproj.orbitflow.global.file.entity.QFile signatureFile;

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee signer;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QDocumentSignature(String variable) {
        this(DocumentSignature.class, forVariable(variable), INITS);
    }

    public QDocumentSignature(Path<? extends DocumentSignature> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDocumentSignature(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDocumentSignature(PathMetadata metadata, PathInits inits) {
        this(DocumentSignature.class, metadata, inits);
    }

    public QDocumentSignature(Class<? extends DocumentSignature> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.approvalLine = inits.isInitialized("approvalLine") ? new com.finalproj.orbitflow.approval.approvalLine.entity.QApprovalLine(forProperty("approvalLine"), inits.get("approvalLine")) : null;
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.document = inits.isInitialized("document") ? new com.finalproj.orbitflow.approval.document.entity.QDocument(forProperty("document"), inits.get("document")) : null;
        this.signatureFile = inits.isInitialized("signatureFile") ? new com.finalproj.orbitflow.global.file.entity.QFile(forProperty("signatureFile"), inits.get("signatureFile")) : null;
        this.signer = inits.isInitialized("signer") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("signer"), inits.get("signer")) : null;
    }

}

