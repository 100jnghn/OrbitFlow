package com.finalproj.orbitflow.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservation is a Querydsl query type for Reservation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservation extends EntityPathBase<Reservation> {

    private static final long serialVersionUID = -1412163839L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservation reservation = new QReservation("reservation");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee employee;

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Integer> endTime = createNumber("endTime", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.finalproj.orbitflow.resource.itemcategory.entity.QItemCategory itemCategory;

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath rejectReason = createString("rejectReason");

    public final DatePath<java.time.LocalDate> reservationDate = createDate("reservationDate", java.time.LocalDate.class);

    public final StringPath reservationReason = createString("reservationReason");

    public final QReservationStatus reservationStatus;

    public final NumberPath<Long> resourceId = createNumber("resourceId", Long.class);

    public final NumberPath<Integer> startTime = createNumber("startTime", Integer.class);

    public final EnumPath<com.finalproj.orbitflow.reservation.enums.ReservationTypeCode> typeCode = createEnum("typeCode", com.finalproj.orbitflow.reservation.enums.ReservationTypeCode.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public QReservation(String variable) {
        this(Reservation.class, forVariable(variable), INITS);
    }

    public QReservation(Path<? extends Reservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservation(PathMetadata metadata, PathInits inits) {
        this(Reservation.class, metadata, inits);
    }

    public QReservation(Class<? extends Reservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.employee = inits.isInitialized("employee") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("employee"), inits.get("employee")) : null;
        this.itemCategory = inits.isInitialized("itemCategory") ? new com.finalproj.orbitflow.resource.itemcategory.entity.QItemCategory(forProperty("itemCategory"), inits.get("itemCategory")) : null;
        this.reservationStatus = inits.isInitialized("reservationStatus") ? new QReservationStatus(forProperty("reservationStatus")) : null;
    }

}

