package com.finalproj.orbitflow.board.boardpermission.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoardPermission is a Querydsl query type for BoardPermission
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoardPermission extends EntityPathBase<BoardPermission> {

    private static final long serialVersionUID = 2001726729L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoardPermission boardPermission = new QBoardPermission("boardPermission");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.board.boardcategory.entity.QBoardCategory boardCategory;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee employee;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QBoardPermission(String variable) {
        this(BoardPermission.class, forVariable(variable), INITS);
    }

    public QBoardPermission(Path<? extends BoardPermission> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoardPermission(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoardPermission(PathMetadata metadata, PathInits inits) {
        this(BoardPermission.class, metadata, inits);
    }

    public QBoardPermission(Class<? extends BoardPermission> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.boardCategory = inits.isInitialized("boardCategory") ? new com.finalproj.orbitflow.board.boardcategory.entity.QBoardCategory(forProperty("boardCategory"), inits.get("boardCategory")) : null;
        this.employee = inits.isInitialized("employee") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("employee"), inits.get("employee")) : null;
    }

}

