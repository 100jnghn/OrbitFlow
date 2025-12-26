package com.finalproj.orbitflow.reservation.repository;

import com.finalproj.orbitflow.reservation.entity.Reservation;
import com.finalproj.orbitflow.reservation.entity.ReservationStatus;
import com.finalproj.orbitflow.reservation.enums.ReservationStatusCode;
import com.finalproj.orbitflow.reservation.enums.ReservationTypeCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
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

    @Query("""
                SELECT r
                FROM Reservation r
                WHERE r.company.id = :companyId
                  AND r.reservationDate = :reservationDate
                  AND r.typeCode = :typeCode
                  AND r.reservationStatus.id = 2
            """)
    List<Reservation> findByCompanyAndDateAndType(
            @Param("companyId") Long companyId,
            @Param("reservationDate") LocalDate reservationDate,
            @Param("typeCode") ReservationTypeCode typeCode
    );


    @Query("""
                select r
                from Reservation r
                where r.company.id = :companyId
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
    Page<Reservation> getReservations(
            @Param("companyId") Long companyId,
            @Param("showPast") boolean showPast,
            @Param("today") LocalDate today,
            @Param("statusId") Long statusId,
            @Param("type") ReservationTypeCode type,
            @Param("deletedStatus") ReservationStatusCode deletedStatus,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update Reservation r
                set r.reservationStatus = :reservationStatus
                where r.id = :reservationId
            """)
    void approveReservation(
            @Param("reservationId") Long reservationId,
            @Param("reservationStatus") ReservationStatus reservationStatus
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update Reservation r
                set r.reservationStatus = :rejectStatus, 
                            r.rejectReason = :rejectReason
                where r.id = :reservationId
            """)
    void rejectReservation(
            @Param("reservationId") Long reservationId,
            @Param("rejectReason") String rejectReason,
            @Param("rejectStatus") ReservationStatus rejectStatus
    );

    // 예약 승인 대기 상태의 Reservation 리스트 조회
    @Query("""
                select r
                from Reservation r
                where r.company.id = :companyId
                  and r.typeCode = :typeCode
                  and r.reservationStatus.statusCode = :statusCode
            """)
    List<Reservation> findWaitingCompanyAndTypeCode(
            @Param("companyId") Long companyId,
            @Param("typeCode") ReservationTypeCode typeCode,
            @Param("statusCode") ReservationStatusCode statusCode
    );

    // 리스트의 reservation id 일괄 승인
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update Reservation r
                set r.reservationStatus = :status
                where r.id in :ids
            """)
    void bulkUpdateStatus(
            @Param("ids") List<Long> ids,
            @Param("status") ReservationStatus status
    );
}
