package com.finalproj.orbitflow.message.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMessage is a Querydsl query type for Message
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMessage extends EntityPathBase<Message> {

    private static final long serialVersionUID = 756779809L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMessage message = new QMessage("message");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final ListPath<com.finalproj.orbitflow.global.file.entity.File, com.finalproj.orbitflow.global.file.entity.QFile> files = this.<com.finalproj.orbitflow.global.file.entity.File, com.finalproj.orbitflow.global.file.entity.QFile>createList("files", com.finalproj.orbitflow.global.file.entity.File.class, com.finalproj.orbitflow.global.file.entity.QFile.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath messageContent = createString("messageContent");

    public final StringPath messageTitle = createString("messageTitle");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee sender;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QMessage(String variable) {
        this(Message.class, forVariable(variable), INITS);
    }

    public QMessage(Path<? extends Message> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMessage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMessage(PathMetadata metadata, PathInits inits) {
        this(Message.class, metadata, inits);
    }

    public QMessage(Class<? extends Message> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.sender = inits.isInitialized("sender") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("sender"), inits.get("sender")) : null;
    }

}

