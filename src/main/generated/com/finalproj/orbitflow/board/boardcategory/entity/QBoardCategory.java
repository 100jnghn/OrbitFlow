package com.finalproj.orbitflow.board.boardcategory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoardCategory is a Querydsl query type for BoardCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoardCategory extends EntityPathBase<BoardCategory> {

    private static final long serialVersionUID = 767282921L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoardCategory boardCategory = new QBoardCategory("boardCategory");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final StringPath boardName = createString("boardName");

    public final ListPath<com.finalproj.orbitflow.board.boardpermission.entity.BoardPermission, com.finalproj.orbitflow.board.boardpermission.entity.QBoardPermission> boardPermissions = this.<com.finalproj.orbitflow.board.boardpermission.entity.BoardPermission, com.finalproj.orbitflow.board.boardpermission.entity.QBoardPermission>createList("boardPermissions", com.finalproj.orbitflow.board.boardpermission.entity.BoardPermission.class, com.finalproj.orbitflow.board.boardpermission.entity.QBoardPermission.class, PathInits.DIRECT2);

    public final StringPath boardType = createString("boardType");

    public final BooleanPath commentActivated = createBoolean("commentActivated");

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final DateTimePath<java.time.Instant> deletedAt = createDateTime("deletedAt", java.time.Instant.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActivated = createBoolean("isActivated");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final com.finalproj.orbitflow.hr.organization.entity.QOrganization organization;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QBoardCategory(String variable) {
        this(BoardCategory.class, forVariable(variable), INITS);
    }

    public QBoardCategory(Path<? extends BoardCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoardCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoardCategory(PathMetadata metadata, PathInits inits) {
        this(BoardCategory.class, metadata, inits);
    }

    public QBoardCategory(Class<? extends BoardCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.organization = inits.isInitialized("organization") ? new com.finalproj.orbitflow.hr.organization.entity.QOrganization(forProperty("organization")) : null;
    }

}

