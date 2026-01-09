package com.finalproj.orbitflow.chatbot.chatbot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChatConversation is a Querydsl query type for ChatConversation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatConversation extends EntityPathBase<ChatConversation> {

    private static final long serialVersionUID = -487011062L;

    public static final QChatConversation chatConversation = new QChatConversation("chatConversation");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final BooleanPath deleted = createBoolean("deleted");

    public final NumberPath<Long> employeeId = createNumber("employeeId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> manualCategoryId = createNumber("manualCategoryId", Long.class);

    public final StringPath manualCategoryName = createString("manualCategoryName");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final EnumPath<ChatConversation.Status> status = createEnum("status", ChatConversation.Status.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QChatConversation(String variable) {
        super(ChatConversation.class, forVariable(variable));
    }

    public QChatConversation(Path<? extends ChatConversation> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChatConversation(PathMetadata metadata) {
        super(ChatConversation.class, metadata);
    }

}

