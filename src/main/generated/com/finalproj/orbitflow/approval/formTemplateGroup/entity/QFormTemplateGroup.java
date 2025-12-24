package com.finalproj.orbitflow.approval.formTemplateGroup.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFormTemplateGroup is a Querydsl query type for FormTemplateGroup
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFormTemplateGroup extends EntityPathBase<FormTemplateGroup> {

    private static final long serialVersionUID = 1849956272L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFormTemplateGroup formTemplateGroup = new QFormTemplateGroup("formTemplateGroup");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final BooleanPath active = createBoolean("active");

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QFormTemplateGroup(String variable) {
        this(FormTemplateGroup.class, forVariable(variable), INITS);
    }

    public QFormTemplateGroup(Path<? extends FormTemplateGroup> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFormTemplateGroup(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFormTemplateGroup(PathMetadata metadata, PathInits inits) {
        this(FormTemplateGroup.class, metadata, inits);
    }

    public QFormTemplateGroup(Class<? extends FormTemplateGroup> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
    }

}

