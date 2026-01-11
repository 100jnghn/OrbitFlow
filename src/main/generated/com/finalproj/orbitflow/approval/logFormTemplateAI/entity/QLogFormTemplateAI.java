package com.finalproj.orbitflow.approval.logFormTemplateAI.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLogFormTemplateAi is a Querydsl query type for LogFormTemplateAi
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLogFormTemplateAi extends EntityPathBase<LogFormTemplateAi> {

    private static final long serialVersionUID = -1271489584L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLogFormTemplateAi logFormTemplateAi = new QLogFormTemplateAi("logFormTemplateAi");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final com.finalproj.orbitflow.approval.formTemplate.entity.QFormTemplate createdTemplate;

    public final StringPath errorMessage = createString("errorMessage");

    public final StringPath generatedTemplateJson = createString("generatedTemplateJson");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath model = createString("model");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath prompt = createString("prompt");

    public final StringPath requestContext = createString("requestContext");

    public final StringPath responseContext = createString("responseContext");

    public final EnumPath<com.finalproj.orbitflow.approval.documentAISummary.enums.AiStatus> status = createEnum("status", com.finalproj.orbitflow.approval.documentAISummary.enums.AiStatus.class);

    public final com.finalproj.orbitflow.approval.formTemplateGroup.entity.QFormTemplateGroup templateGroup;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QLogFormTemplateAi(String variable) {
        this(LogFormTemplateAi.class, forVariable(variable), INITS);
    }

    public QLogFormTemplateAi(Path<? extends LogFormTemplateAi> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLogFormTemplateAi(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLogFormTemplateAi(PathMetadata metadata, PathInits inits) {
        this(LogFormTemplateAi.class, metadata, inits);
    }

    public QLogFormTemplateAi(Class<? extends LogFormTemplateAi> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.createdTemplate = inits.isInitialized("createdTemplate") ? new com.finalproj.orbitflow.approval.formTemplate.entity.QFormTemplate(forProperty("createdTemplate"), inits.get("createdTemplate")) : null;
        this.templateGroup = inits.isInitialized("templateGroup") ? new com.finalproj.orbitflow.approval.formTemplateGroup.entity.QFormTemplateGroup(forProperty("templateGroup"), inits.get("templateGroup")) : null;
    }

}

