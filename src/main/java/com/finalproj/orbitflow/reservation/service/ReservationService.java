package com.finalproj.orbitflow.reservation.service;

import com.finalproj.orbitflow.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationService
 * @since : 2025-12-22 오후 2:21 월요일
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
}
