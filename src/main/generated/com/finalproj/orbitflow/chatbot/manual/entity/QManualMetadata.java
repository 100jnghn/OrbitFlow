package com.finalproj.orbitflow.chatbot.manual.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QManualMetadata is a Querydsl query type for ManualMetadata
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QManualMetadata extends EntityPathBase<ManualMetadata> {

    private static final long serialVersionUID = 1756625301L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QManualMetadata manualMetadata = new QManualMetadata("manualMetadata");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.chatbot.manualCategory.entity.QManualCategory category;

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final com.finalproj.orbitflow.global.file.entity.QFile file;

    public final StringPath fileName = createString("fileName");

    public final StringPath filePath = createString("filePath");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath status = createString("status");

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QManualMetadata(String variable) {
        this(ManualMetadata.class, forVariable(variable), INITS);
    }

    public QManualMetadata(Path<? extends ManualMetadata> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QManualMetadata(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QManualMetadata(PathMetadata metadata, PathInits inits) {
        this(ManualMetadata.class, metadata, inits);
    }

    public QManualMetadata(Class<? extends ManualMetadata> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.finalproj.orbitflow.chatbot.manualCategory.entity.QManualCategory(forProperty("category"), inits.get("category")) : null;
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.file = inits.isInitialized("file") ? new com.finalproj.orbitflow.global.file.entity.QFile(forProperty("file"), inits.get("file")) : null;
    }

}

