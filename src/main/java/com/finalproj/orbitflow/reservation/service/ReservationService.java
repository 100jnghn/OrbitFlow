package com.finalproj.orbitflow.reservation.service;

import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.reservation.dto.ReservationReqDto;
import com.finalproj.orbitflow.reservation.dto.ReservationResDto;
import com.finalproj.orbitflow.reservation.entity.Reservation;
import com.finalproj.orbitflow.reservation.entity.ReservationStatus;
import com.finalproj.orbitflow.reservation.enums.ReservationStatusCode;
import com.finalproj.orbitflow.reservation.enums.ReservationTypeCode;
import com.finalproj.orbitflow.reservation.repository.ReservationRepository;
import com.finalproj.orbitflow.reservation.repository.ReservationStatusRepository;
import com.finalproj.orbitflow.resource.car.repository.CarRepository;
import com.finalproj.orbitflow.resource.itemcategory.entity.ItemCategory;
import com.finalproj.orbitflow.resource.itemcategory.repository.ItemCategoryRepository;
import com.finalproj.orbitflow.resource.meetingroom.repository.MeetingroomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
    private final ReservationStatusRepository reservationStatusRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final MeetingroomRepository meetingroomRepository;
    private final CarRepository carRepository;


    @Transactional(readOnly = true)
    public Page<ReservationResDto> getMyReservations(Long employeeId, boolean showPast, Long statusId, String typeCode, Pageable pageable) {

        // typeCode -> Enum 변환
        ReservationTypeCode type = null;
        if (typeCode != null) {

            type = ReservationTypeCode.valueOf(typeCode.toUpperCase());
            log.info("예약 상태 : " + type);
        }

        Long status = null;
        if (statusId != null) {
            status = statusId;
        }

        LocalDate today = LocalDate.now();

        // showPast = true  -> 과거 예약
        // showPast = false -> 오늘 이후 예약
        return reservationRepository.getMyReservations(
                employeeId,
                showPast,
                today,
                status,
                type,
                ReservationStatusCode.DELETED,
                pageable
        ).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public ReservationResDto getMyReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없음"));

        return convertToDto(reservation);
    }


    @Transactional
    public void insertReservation(Long companyId, Long userId, ReservationReqDto reservation) {

        Company company = companyRepository.getReferenceById(companyId);
        Employee employee = employeeRepository.getReferenceById(userId);

        ReservationTypeCode reservationTypeCode;
        String typeCode = reservation.getTypeCode();
        ItemCategory itemCategory = null;
        ReservationStatus reservationStatus = null;

        // 회의실 예약
        if (typeCode.equals("MEETING")) {
            reservationTypeCode = ReservationTypeCode.MEETING;
            reservationStatus = reservationStatusRepository.getReferenceById(2L);   // 2 = 예약 확정
        } 
        // 차량 예약
        else if (typeCode.equals("CAR")) {
            reservationTypeCode = ReservationTypeCode.CAR;
            reservationStatus = reservationStatusRepository.getReferenceById(1L);   // 1 = 예약 대기
        } 
        // 기타 자원 예약
        else {
            reservationTypeCode = ReservationTypeCode.ITEM;
            itemCategory = itemCategoryRepository.getReferenceById(reservation.getItemCategoryId());
            reservationStatus = reservationStatusRepository.getReferenceById(1L);   // 1 = 예약 대기
        }

        Reservation newReservation = Reservation.builder()
                .company(company)
                .employee(employee)
                .typeCode(reservationTypeCode)
                .itemCategory(itemCategory)
                .resourceId(reservation.getResourceId())
                .reservationDate(reservation.getReservationDate())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .reservationReason(reservation.getReservationReason())
                .reservationStatus(reservationStatus)
                .build();

        reservationRepository.save(newReservation);
    }


    @Transactional
    public void cancelReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약 찾을 수 없음"));

        ReservationStatusCode current = reservation.getReservationStatus().getStatusCode();

        // 이미 취소 or 삭제된 상태
        if (current == ReservationStatusCode.CANCELED
                || current == ReservationStatusCode.DELETED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        ReservationStatus canceledStatus = reservationStatusRepository.findByStatusCode(ReservationStatusCode.CANCELED);

        // 취소 상태로 변경
        reservation.changeStatus(canceledStatus);
    }

    // Entity -> Dto
    private ReservationResDto convertToDto(Reservation reservation) {

        Employee employee = reservation.getEmployee();
        ItemCategory itemCategory = reservation.getItemCategory();
        Long itemCategoryId = null;
        String itemCategoryName = null;

        if (itemCategory != null) {
            itemCategoryId = itemCategory.getId();
            itemCategoryName = itemCategory.getName();
        }

        ReservationStatus reservationStatus = reservation.getReservationStatus();
        Long reservationStatusId = reservationStatus.getId();
        String reservationStatusName = reservationStatus.getStatusName();
        String typeName = reservation.getTypeCode().getDescription();

        String resourceName = "";

        if(reservation.getTypeCode() == ReservationTypeCode.MEETING) {
            resourceName = meetingroomRepository.findById(reservation.getResourceId()).get().getName();
        }
        else if (reservation.getTypeCode() == ReservationTypeCode.CAR) {
            resourceName = carRepository.findById(reservation.getResourceId()).get().getName();
        }
        else if (reservation.getTypeCode() ==  ReservationTypeCode.ITEM) {
            resourceName = itemCategoryRepository.findById(reservation.getResourceId()).get().getName();
        }

        return ReservationResDto.builder()
                .reservationId(reservation.getId())
                .employeeName(employee.getName())
                .organizationName(employee.getOrganization().getName())
                .typeCode(reservation.getTypeCode())
                .typeName(typeName)
                .itemCategoryId(itemCategoryId)
                .itemCategoryName(itemCategoryName)
                .resourceId(reservation.getResourceId())
                .resourceName(resourceName)
                .reservationDate(reservation.getReservationDate())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .reservationReason(reservation.getReservationReason())
                .rejectReason(reservation.getRejectReason())
                .reservationStatusId(reservationStatusId)
                .reservationStatusName(reservationStatusName)
                .build();
    }

}
