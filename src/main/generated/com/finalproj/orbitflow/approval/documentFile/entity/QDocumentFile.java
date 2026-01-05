package com.finalproj.orbitflow.approval.documentFile.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDocumentFile is a Querydsl query type for DocumentFile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDocumentFile extends EntityPathBase<DocumentFile> {

    private static final long serialVersionUID = 1508074072L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDocumentFile documentFile = new QDocumentFile("documentFile");

    public final com.finalproj.orbitflow.approval.document.entity.QDocument document;

    public final com.finalproj.orbitflow.global.file.entity.QFile file;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> referenceTargetId = createNumber("referenceTargetId", Long.class);

    public final EnumPath<com.finalproj.orbitflow.approval.documentFile.enums.ReferenceType> referenceType = createEnum("referenceType", com.finalproj.orbitflow.approval.documentFile.enums.ReferenceType.class);

    public final StringPath referenceUrl = createString("referenceUrl");

    public QDocumentFile(String variable) {
        this(DocumentFile.class, forVariable(variable), INITS);
    }

    public QDocumentFile(Path<? extends DocumentFile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDocumentFile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDocumentFile(PathMetadata metadata, PathInits inits) {
        this(DocumentFile.class, metadata, inits);
    }

    public QDocumentFile(Class<? extends DocumentFile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.document = inits.isInitialized("document") ? new com.finalproj.orbitflow.approval.document.entity.QDocument(forProperty("document"), inits.get("document")) : null;
        this.file = inits.isInitialized("file") ? new com.finalproj.orbitflow.global.file.entity.QFile(forProperty("file"), inits.get("file")) : null;
    }

}

