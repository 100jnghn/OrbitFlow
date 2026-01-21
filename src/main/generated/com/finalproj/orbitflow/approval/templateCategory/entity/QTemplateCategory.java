package com.finalproj.orbitflow.approval.form.template.category.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTemplateCategory is a Querydsl query type for TemplateCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTemplateCategory extends EntityPathBase<TemplateCategory> {

    private static final long serialVersionUID = -185547494L;

    public static final QTemplateCategory templateCategory = new QTemplateCategory("category");

    public final EnumPath<com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode> code = createEnum("code", com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public QTemplateCategory(String variable) {
        super(TemplateCategory.class, forVariable(variable));
    }

    public QTemplateCategory(Path<? extends TemplateCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTemplateCategory(PathMetadata metadata) {
        super(TemplateCategory.class, metadata);
    }

}

