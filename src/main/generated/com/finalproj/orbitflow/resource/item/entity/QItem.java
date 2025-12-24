package com.finalproj.orbitflow.resource.item.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QItem is a Querydsl query type for Item
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QItem extends EntityPathBase<Item> {

    private static final long serialVersionUID = -1357536347L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QItem item = new QItem("item");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final StringPath description = createString("description");

    public final com.finalproj.orbitflow.global.file.entity.QFile file;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.finalproj.orbitflow.resource.itemcategory.entity.QItemCategory itemCategory;

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath name = createString("name");

    public final com.finalproj.orbitflow.resource.status.entity.QResourceStatus resourceStatus;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QItem(String variable) {
        this(Item.class, forVariable(variable), INITS);
    }

    public QItem(Path<? extends Item> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QItem(PathMetadata metadata, PathInits inits) {
        this(Item.class, metadata, inits);
    }

    public QItem(Class<? extends Item> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.file = inits.isInitialized("file") ? new com.finalproj.orbitflow.global.file.entity.QFile(forProperty("file"), inits.get("file")) : null;
        this.itemCategory = inits.isInitialized("itemCategory") ? new com.finalproj.orbitflow.resource.itemcategory.entity.QItemCategory(forProperty("itemCategory"), inits.get("itemCategory")) : null;
        this.resourceStatus = inits.isInitialized("resourceStatus") ? new com.finalproj.orbitflow.resource.status.entity.QResourceStatus(forProperty("resourceStatus")) : null;
    }

}

