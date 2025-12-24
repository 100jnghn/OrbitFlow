package com.finalproj.orbitflow.hr.orgPositionUsage.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrgPositionUsage is a Querydsl query type for OrgPositionUsage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrgPositionUsage extends EntityPathBase<OrgPositionUsage> {

    private static final long serialVersionUID = 2050644843L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrgPositionUsage orgPositionUsage = new QOrgPositionUsage("orgPositionUsage");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final com.finalproj.orbitflow.hr.organization.entity.QOrganization organization;

    public final com.finalproj.orbitflow.hr.positionCategory.entity.QPositionCategory positionCategory;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QOrgPositionUsage(String variable) {
        this(OrgPositionUsage.class, forVariable(variable), INITS);
    }

    public QOrgPositionUsage(Path<? extends OrgPositionUsage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrgPositionUsage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrgPositionUsage(PathMetadata metadata, PathInits inits) {
        this(OrgPositionUsage.class, metadata, inits);
    }

    public QOrgPositionUsage(Class<? extends OrgPositionUsage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.organization = inits.isInitialized("organization") ? new com.finalproj.orbitflow.hr.organization.entity.QOrganization(forProperty("organization")) : null;
        this.positionCategory = inits.isInitialized("positionCategory") ? new com.finalproj.orbitflow.hr.positionCategory.entity.QPositionCategory(forProperty("positionCategory"), inits.get("positionCategory")) : null;
    }

}

