package com.finalproj.orbitflow.resource.meetingroom.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMeetingroom is a Querydsl query type for Meetingroom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMeetingroom extends EntityPathBase<Meetingroom> {

    private static final long serialVersionUID = -1513174171L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMeetingroom meetingroom = new QMeetingroom("meetingroom");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath name = createString("name");

    public final StringPath position = createString("position");

    public final com.finalproj.orbitflow.resource.status.entity.QResourceStatus resourceStatus;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QMeetingroom(String variable) {
        this(Meetingroom.class, forVariable(variable), INITS);
    }

    public QMeetingroom(Path<? extends Meetingroom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMeetingroom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMeetingroom(PathMetadata metadata, PathInits inits) {
        this(Meetingroom.class, metadata, inits);
    }

    public QMeetingroom(Class<? extends Meetingroom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.resourceStatus = inits.isInitialized("resourceStatus") ? new com.finalproj.orbitflow.resource.status.entity.QResourceStatus(forProperty("resourceStatus")) : null;
    }

}

