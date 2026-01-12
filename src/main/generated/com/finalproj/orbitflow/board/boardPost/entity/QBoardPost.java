package com.finalproj.orbitflow.board.boardPost.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoardPost is a Querydsl query type for BoardPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoardPost extends EntityPathBase<BoardPost> {

    private static final long serialVersionUID = 1370010761L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoardPost boardPost = new QBoardPost("boardPost");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final StringPath boardContent = createString("boardContent");

    public final StringPath boardTitle = createString("boardTitle");

    public final com.finalproj.orbitflow.board.boardCategory.entity.QBoardCategory category;

    public final NumberPath<Integer> commentCount = createNumber("commentCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final DateTimePath<java.time.Instant> deletedAt = createDateTime("deletedAt", java.time.Instant.class);

    public final ListPath<com.finalproj.orbitflow.global.file.entity.File, com.finalproj.orbitflow.global.file.entity.QFile> files = this.<com.finalproj.orbitflow.global.file.entity.File, com.finalproj.orbitflow.global.file.entity.QFile>createList("files", com.finalproj.orbitflow.global.file.entity.File.class, com.finalproj.orbitflow.global.file.entity.QFile.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee writer;

    public QBoardPost(String variable) {
        this(BoardPost.class, forVariable(variable), INITS);
    }

    public QBoardPost(Path<? extends BoardPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoardPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoardPost(PathMetadata metadata, PathInits inits) {
        this(BoardPost.class, metadata, inits);
    }

    public QBoardPost(Class<? extends BoardPost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.finalproj.orbitflow.board.boardCategory.entity.QBoardCategory(forProperty("category"), inits.get("category")) : null;
        this.writer = inits.isInitialized("writer") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("writer"), inits.get("writer")) : null;
    }

}

