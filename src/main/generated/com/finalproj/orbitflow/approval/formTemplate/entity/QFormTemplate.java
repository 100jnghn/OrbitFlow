package com.finalproj.orbitflow.approval.formTemplate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFormTemplate is a Querydsl query type for FormTemplate
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFormTemplate extends EntityPathBase<FormTemplate> {

    private static final long serialVersionUID = 1953419558L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFormTemplate formTemplate = new QFormTemplate("formTemplate");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final NumberPath<Integer> activeVersion = createNumber("activeVersion", Integer.class);

    public final ListPath<com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag, EnumPath<com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag>> affectTags = this.<com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag, EnumPath<com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag>>createList("affectTags", com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag.class, EnumPath.class, PathInits.DIRECT2);

    public final StringPath approvalRuleJson = createString("approvalRuleJson");

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final EnumPath<com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus> status = createEnum("status", com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus.class);

    public final com.finalproj.orbitflow.approval.formTemplateGroup.entity.QFormTemplateGroup templateGroup;

    public final StringPath templateJson = createString("templateJson");

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QFormTemplate(String variable) {
        this(FormTemplate.class, forVariable(variable), INITS);
    }

    public QFormTemplate(Path<? extends FormTemplate> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFormTemplate(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFormTemplate(PathMetadata metadata, PathInits inits) {
        this(FormTemplate.class, metadata, inits);
    }

    public QFormTemplate(Class<? extends FormTemplate> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.templateGroup = inits.isInitialized("templateGroup") ? new com.finalproj.orbitflow.approval.formTemplateGroup.entity.QFormTemplateGroup(forProperty("templateGroup"), inits.get("templateGroup")) : null;
    }

}

