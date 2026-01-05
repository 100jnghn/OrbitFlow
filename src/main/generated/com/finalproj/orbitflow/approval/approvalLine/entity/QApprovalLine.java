package com.finalproj.orbitflow.approval.approvalLine.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QApprovalLine is a Querydsl query type for ApprovalLine
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApprovalLine extends EntityPathBase<ApprovalLine> {

    private static final long serialVersionUID = 868895128L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QApprovalLine approvalLine = new QApprovalLine("approvalLine");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee approver;

    public final StringPath comment = createString("comment");

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final DateTimePath<java.time.LocalDateTime> decidedAt = createDateTime("decidedAt", java.time.LocalDateTime.class);

    public final com.finalproj.orbitflow.approval.document.entity.QDocument document;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final NumberPath<Integer> orderNo = createNumber("orderNo", Integer.class);

    public final com.finalproj.orbitflow.hr.organization.entity.QOrganization organization;

    public final com.finalproj.orbitflow.hr.positionCategory.entity.QPositionCategory positionCategory;

    public final EnumPath<com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus> status = createEnum("status", com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QApprovalLine(String variable) {
        this(ApprovalLine.class, forVariable(variable), INITS);
    }

    public QApprovalLine(Path<? extends ApprovalLine> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QApprovalLine(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QApprovalLine(PathMetadata metadata, PathInits inits) {
        this(ApprovalLine.class, metadata, inits);
    }

    public QApprovalLine(Class<? extends ApprovalLine> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.approver = inits.isInitialized("approver") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("approver"), inits.get("approver")) : null;
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.document = inits.isInitialized("document") ? new com.finalproj.orbitflow.approval.document.entity.QDocument(forProperty("document"), inits.get("document")) : null;
        this.organization = inits.isInitialized("organization") ? new com.finalproj.orbitflow.hr.organization.entity.QOrganization(forProperty("organization")) : null;
        this.positionCategory = inits.isInitialized("positionCategory") ? new com.finalproj.orbitflow.hr.positionCategory.entity.QPositionCategory(forProperty("positionCategory"), inits.get("positionCategory")) : null;
    }

}

