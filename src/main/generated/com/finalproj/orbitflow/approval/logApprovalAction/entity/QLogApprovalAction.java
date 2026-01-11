package com.finalproj.orbitflow.approval.logApprovalAction.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLogApprovalAction is a Querydsl query type for LogApprovalAction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLogApprovalAction extends EntityPathBase<LogApprovalAction> {

    private static final long serialVersionUID = -1107285200L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLogApprovalAction logApprovalAction = new QLogApprovalAction("logApprovalAction");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final EnumPath<com.finalproj.orbitflow.approval.logApprovalAction.enums.ApprovalAction> action = createEnum("action", com.finalproj.orbitflow.approval.logApprovalAction.enums.ApprovalAction.class);

    public final com.finalproj.orbitflow.approval.approvalLine.entity.QApprovalLine approvalLine;

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee approver;

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final com.finalproj.orbitflow.approval.document.entity.QDocument document;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final NumberPath<Integer> step = createNumber("step", Integer.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QLogApprovalAction(String variable) {
        this(LogApprovalAction.class, forVariable(variable), INITS);
    }

    public QLogApprovalAction(Path<? extends LogApprovalAction> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLogApprovalAction(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLogApprovalAction(PathMetadata metadata, PathInits inits) {
        this(LogApprovalAction.class, metadata, inits);
    }

    public QLogApprovalAction(Class<? extends LogApprovalAction> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.approvalLine = inits.isInitialized("approvalLine") ? new com.finalproj.orbitflow.approval.approvalLine.entity.QApprovalLine(forProperty("approvalLine"), inits.get("approvalLine")) : null;
        this.approver = inits.isInitialized("approver") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("approver"), inits.get("approver")) : null;
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.document = inits.isInitialized("document") ? new com.finalproj.orbitflow.approval.document.entity.QDocument(forProperty("document"), inits.get("document")) : null;
    }

}

