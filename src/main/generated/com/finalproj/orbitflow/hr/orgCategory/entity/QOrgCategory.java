package com.finalproj.orbitflow.hr.orgCategory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrgCategory is a Querydsl query type for OrgCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrgCategory extends EntityPathBase<OrgCategory> {

    private static final long serialVersionUID = 6188265L;

    public static final QOrgCategory orgCategory = new QOrgCategory("orgCategory");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final BooleanPath isRoot = createBoolean("isRoot");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath name = createString("name");

    public final NumberPath<Integer> orderIndex = createNumber("orderIndex", Integer.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QOrgCategory(String variable) {
        super(OrgCategory.class, forVariable(variable));
    }

    public QOrgCategory(Path<? extends OrgCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrgCategory(PathMetadata metadata) {
        super(OrgCategory.class, metadata);
    }

}

