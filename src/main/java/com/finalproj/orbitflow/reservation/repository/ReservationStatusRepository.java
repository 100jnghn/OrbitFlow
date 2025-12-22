package com.finalproj.orbitflow.reservation.repository;

import com.finalproj.orbitflow.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationStatusRepository
 * @since : 2025-12-22 오후 2:17 월요일
 */
public interface ReservationStatusRepository extends JpaRepository<ReservationStatus, Long> {

}
