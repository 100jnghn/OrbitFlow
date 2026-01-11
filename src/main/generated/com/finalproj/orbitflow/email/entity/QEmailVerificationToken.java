package com.finalproj.orbitflow.email.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEmailVerificationToken is a Querydsl query type for EmailVerificationToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmailVerificationToken extends EntityPathBase<EmailVerificationToken> {

    private static final long serialVersionUID = 1352969053L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEmailVerificationToken emailVerificationToken = new QEmailVerificationToken("emailVerificationToken");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee employee;

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath token = createString("token");

    public final EnumPath<com.finalproj.orbitflow.email.enums.EmailTokenType> type = createEnum("type", com.finalproj.orbitflow.email.enums.EmailTokenType.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public final DateTimePath<java.time.LocalDateTime> usedAt = createDateTime("usedAt", java.time.LocalDateTime.class);

    public QEmailVerificationToken(String variable) {
        this(EmailVerificationToken.class, forVariable(variable), INITS);
    }

    public QEmailVerificationToken(Path<? extends EmailVerificationToken> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEmailVerificationToken(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEmailVerificationToken(PathMetadata metadata, PathInits inits) {
        this(EmailVerificationToken.class, metadata, inits);
    }

    public QEmailVerificationToken(Class<? extends EmailVerificationToken> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.employee = inits.isInitialized("employee") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("employee"), inits.get("employee")) : null;
    }

}

