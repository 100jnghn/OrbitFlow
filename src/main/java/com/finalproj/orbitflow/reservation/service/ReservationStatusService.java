package com.finalproj.orbitflow.reservation.service;

import com.finalproj.orbitflow.reservation.dto.ReservationStatusResDto;
import com.finalproj.orbitflow.reservation.repository.ReservationStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationStatusService
 * @since : 2025-12-22 오후 2:21 월요일
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationStatusService {

    private final ReservationStatusRepository reservationStatusRepository;

    @Transactional(readOnly = true)
    public List<ReservationStatusResDto> getReservationStatuses() {

        return reservationStatusRepository.findAll()
                .stream()
                .map(ReservationStatusResDto::fromEntity)
                .collect(Collectors.toList());
    }

}
