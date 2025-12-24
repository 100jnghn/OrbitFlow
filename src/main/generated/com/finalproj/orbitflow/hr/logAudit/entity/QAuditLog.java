package com.finalproj.orbitflow.hr.logAudit.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAuditLog is a Querydsl query type for AuditLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAuditLog extends EntityPathBase<AuditLog> {

    private static final long serialVersionUID = 273833955L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAuditLog auditLog = new QAuditLog("auditLog");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee actor;

    public final StringPath afterData = createString("afterData");

    public final StringPath beforeData = createString("beforeData");

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> entityId = createNumber("entityId", Long.class);

    public final EnumPath<com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType> entityType = createEnum("entityType", com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType.class);

    public final EnumPath<com.finalproj.orbitflow.hr.logAudit.enums.AuditEventType> eventType = createEnum("eventType", com.finalproj.orbitflow.hr.logAudit.enums.AuditEventType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QAuditLog(String variable) {
        this(AuditLog.class, forVariable(variable), INITS);
    }

    public QAuditLog(Path<? extends AuditLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAuditLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAuditLog(PathMetadata metadata, PathInits inits) {
        this(AuditLog.class, metadata, inits);
    }

    public QAuditLog(Class<? extends AuditLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.actor = inits.isInitialized("actor") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("actor"), inits.get("actor")) : null;
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
    }

}

