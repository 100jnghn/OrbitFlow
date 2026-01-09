package com.finalproj.orbitflow.chatbot.chatbot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChatMessage is a Querydsl query type for ChatMessage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatMessage extends EntityPathBase<ChatMessage> {

    private static final long serialVersionUID = -989882592L;

    public static final QChatMessage chatMessage = new QChatMessage("chatMessage");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final NumberPath<Long> companyId = createNumber("companyId", Long.class);

    public final StringPath content = createString("content");

    public final NumberPath<Long> conversationId = createNumber("conversationId", Long.class);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final SimplePath<com.fasterxml.jackson.databind.JsonNode> metaJson = createSimple("metaJson", com.fasterxml.jackson.databind.JsonNode.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final EnumPath<ChatMessage.Role> role = createEnum("role", ChatMessage.Role.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QChatMessage(String variable) {
        super(ChatMessage.class, forVariable(variable));
    }

    public QChatMessage(Path<? extends ChatMessage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChatMessage(PathMetadata metadata) {
        super(ChatMessage.class, metadata);
    }

}

