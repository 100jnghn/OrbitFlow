package com.finalproj.orbitflow.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReservationStatus is a Querydsl query type for ReservationStatus
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservationStatus extends EntityPathBase<ReservationStatus> {

    private static final long serialVersionUID = -180367789L;

    public static final QReservationStatus reservationStatus = new QReservationStatus("reservationStatus");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.finalproj.orbitflow.reservation.enums.ReservationStatusCode> statusCode = createEnum("statusCode", com.finalproj.orbitflow.reservation.enums.ReservationStatusCode.class);

    public final StringPath statusName = createString("statusName");

    public QReservationStatus(String variable) {
        super(ReservationStatus.class, forVariable(variable));
    }

    public QReservationStatus(Path<? extends ReservationStatus> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReservationStatus(PathMetadata metadata) {
        super(ReservationStatus.class, metadata);
    }

}

