package com.finalproj.orbitflow.resource.car.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCar is a Querydsl query type for Car
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCar extends EntityPathBase<Car> {

    private static final long serialVersionUID = 535173541L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCar car = new QCar("car");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final StringPath description = createString("description");

    public final NumberPath<Integer> driverAge = createNumber("driverAge", Integer.class);

    public final com.finalproj.orbitflow.global.file.entity.QFile file;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath name = createString("name");

    public final StringPath number = createString("number");

    public final com.finalproj.orbitflow.resource.status.entity.QResourceStatus resourceStatus;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QCar(String variable) {
        this(Car.class, forVariable(variable), INITS);
    }

    public QCar(Path<? extends Car> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCar(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCar(PathMetadata metadata, PathInits inits) {
        this(Car.class, metadata, inits);
    }

    public QCar(Class<? extends Car> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.file = inits.isInitialized("file") ? new com.finalproj.orbitflow.global.file.entity.QFile(forProperty("file"), inits.get("file")) : null;
        this.resourceStatus = inits.isInitialized("resourceStatus") ? new com.finalproj.orbitflow.resource.status.entity.QResourceStatus(forProperty("resourceStatus")) : null;
    }

}

