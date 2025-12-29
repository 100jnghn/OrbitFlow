package com.finalproj.orbitflow.message.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMessageRecipient is a Querydsl query type for MessageRecipient
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMessageRecipient extends EntityPathBase<MessageRecipient> {

    private static final long serialVersionUID = 1736178680L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMessageRecipient messageRecipient = new QMessageRecipient("messageRecipient");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final DateTimePath<java.time.Instant> deletedAt = createDateTime("deletedAt", java.time.Instant.class);

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee employee;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isArchived = createBoolean("isArchived");

    public final BooleanPath isRead = createBoolean("isRead");

    public final QMessage message;

    public final EnumPath<com.finalproj.orbitflow.message.enums.MessageFolderType> messageFolderType = createEnum("messageFolderType", com.finalproj.orbitflow.message.enums.MessageFolderType.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final DateTimePath<java.time.Instant> readAt = createDateTime("readAt", java.time.Instant.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QMessageRecipient(String variable) {
        this(MessageRecipient.class, forVariable(variable), INITS);
    }

    public QMessageRecipient(Path<? extends MessageRecipient> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMessageRecipient(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMessageRecipient(PathMetadata metadata, PathInits inits) {
        this(MessageRecipient.class, metadata, inits);
    }

    public QMessageRecipient(Class<? extends MessageRecipient> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.employee = inits.isInitialized("employee") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("employee"), inits.get("employee")) : null;
        this.message = inits.isInitialized("message") ? new QMessage(forProperty("message"), inits.get("message")) : null;
    }

}

