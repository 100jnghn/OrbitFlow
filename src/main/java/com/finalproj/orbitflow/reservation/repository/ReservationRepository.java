package com.finalproj.orbitflow.reservation.repository;

import com.finalproj.orbitflow.reservation.entity.Reservation;
import com.finalproj.orbitflow.reservation.enums.ReservationStatusCode;
import com.finalproj.orbitflow.reservation.enums.ReservationTypeCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationRepository
 * @since : 2025-12-22 오후 2:17 월요일
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    @Query("""
                select r
                from Reservation r
                where r.employee.id = :employeeId
                  and r.reservationStatus.statusCode <> :deletedStatus
            
                  and (
                        (:showPast = true  and r.reservationDate < :today)
                     or (:showPast = false and r.reservationDate >= :today)
                  )
            
                  and (
                        :statusId is null
                        or r.reservationStatus.id = :statusId
                  )
            
                  and (
                        :type is null
                        or r.typeCode = :type
                  )
            """)
    Page<Reservation> getMyReservations(
            @Param("employeeId") Long employeeId,
            @Param("showPast") boolean showPast,
            @Param("today") LocalDate today,
            @Param("statusId") Long statusId,
            @Param("type") ReservationTypeCode type,
            @Param("deletedStatus") ReservationStatusCode deletedStatus,
            Pageable pageable
    );


    Optional<Reservation> findById(Long reservationId);
}
