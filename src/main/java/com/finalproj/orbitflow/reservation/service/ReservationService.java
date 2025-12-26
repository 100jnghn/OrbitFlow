package com.finalproj.orbitflow.reservation.service;

import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.reservation.dto.ReservationReqDto;
import com.finalproj.orbitflow.reservation.dto.ReservationResDto;
import com.finalproj.orbitflow.reservation.dto.ReservationStatusChangeReqDto;
import com.finalproj.orbitflow.reservation.entity.Reservation;
import com.finalproj.orbitflow.reservation.entity.ReservationStatus;
import com.finalproj.orbitflow.reservation.enums.ReservationStatusCode;
import com.finalproj.orbitflow.reservation.enums.ReservationTypeCode;
import com.finalproj.orbitflow.reservation.repository.ReservationRepository;
import com.finalproj.orbitflow.reservation.repository.ReservationStatusRepository;
import com.finalproj.orbitflow.resource.car.repository.CarRepository;
import com.finalproj.orbitflow.resource.item.repository.ItemRepository;
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
import java.util.*;
import java.util.stream.Collectors;

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
    private final ItemRepository itemRepository;


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

//        log.info("예약 타입 : " + reservation.getTypeCode());

        Company company = companyRepository.getReferenceById(companyId);
        Employee employee = employeeRepository.getReferenceById(userId);

        ReservationTypeCode reservationTypeCode;
        String typeCode = reservation.getTypeCode();
        ItemCategory itemCategory = null;
        ReservationStatus reservationStatus = null;
        LocalDate endDate = reservation.getReservationDate();   // 기본은 시작일 = 종료일

        // 회의실 예약
        if (typeCode.equals("MEETING")) {
            reservationTypeCode = ReservationTypeCode.MEETING;
            reservationStatus = reservationStatusRepository.getReferenceById(2L);   // 2 = 예약 확정
        }
        // 차량 예약
        else if (typeCode.equals("CAR")) {
            reservationTypeCode = ReservationTypeCode.CAR;
            reservationStatus = reservationStatusRepository.getReferenceById(1L);   // 1 = 예약 대기

            // 차량 예약은 endDate가 따로 있음
            endDate = reservation.getEndDate();
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
                .endDate(endDate)
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .reservationReason(reservation.getReservationReason())
                .reservationStatus(reservationStatus)
                .build();

        reservationRepository.save(newReservation);
    }

    // 사용자 - 예약 취소 -> 사용자 예약 취소는 사유를 입력받지 않음
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
        reservation.changeStatus(canceledStatus, null);
    }

    @Transactional(readOnly = true)
    public List<ReservationResDto> getReservationsByDate(Long companyId, String date, String typeCode) {

        LocalDate targetDate = LocalDate.parse(date);
        ReservationTypeCode type = ReservationTypeCode.valueOf(typeCode.toUpperCase());

        return reservationRepository.findByCompanyAndDateAndType(
                companyId,
                targetDate,
                type
        ).stream().map(this::convertToDto).toList();
    }


    @Transactional(readOnly = true)
    public Page<ReservationResDto> getReservations(
            Long companyId,
            boolean showPast,
            Long statusId,
            String typeCode,
            Pageable pageable
    ) {
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
        return reservationRepository.getReservations(
                companyId,
                showPast,
                today,
                status,
                type,
                ReservationStatusCode.DELETED,
                pageable
        ).map(this::convertToDto);
    }

    @Transactional
    public void approveReservation(Long reservationId) {

        ReservationStatus confirmStatus = reservationStatusRepository.findByStatusCode(ReservationStatusCode.CONFIRM);
        reservationRepository.approveReservation(reservationId, confirmStatus);
    }

    @Transactional
    public void rejectReservation(Long reservationId, ReservationStatusChangeReqDto rejectReqDto) {

        String rejectReason = rejectReqDto.getRejectReason();

        ReservationStatus rejectStatus = reservationStatusRepository.findByStatusCode(ReservationStatusCode.REJECT);
        reservationRepository.rejectReservation(reservationId, rejectReason, rejectStatus);
    }

    // 관리자 - 예약 상태 변경 -> 반려/취소는 사유를 필수 입력해야 함
    @Transactional
    public void changeReservationStatus(Long reservationId, ReservationStatusChangeReqDto dto) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("해당 예약을 찾을 수 없습니다."));

        ReservationStatus newStatus = reservationStatusRepository.findById(dto.getStatusId())
                .orElseThrow(() -> new IllegalStateException("해당 상태로 변경 불가"));

        ReservationStatusCode targetCode = newStatus.getStatusCode();

        String rejectReason = null;
        if (targetCode.equals(ReservationStatusCode.REJECT) || targetCode.equals(ReservationStatusCode.CANCELED)) {
            if (dto.getRejectReason() == null || dto.getRejectReason().isBlank()) {
                throw new IllegalArgumentException("반려/취소 사유는 필수입니다.");
            }
            rejectReason = dto.getRejectReason();
        }

        reservation.changeStatus(newStatus, rejectReason);
    }


    // ---------------------------------------------------------------------------------- //
    // region 예약 일괄 승인 관련 함수

    /*
     * @param companyId
     * @param typeCode
     * @return -> 일괄 승인한 예약 수
     *
     * typeCode = 'ALL' || 'CAR' || 'ITEM'
     */
    @Transactional
    public int batchApprove(Long companyId, String typeCode) {

        int result = 0;

        if (typeCode.equals("ALL")) {
            result += carBatchApprove(companyId);
            result += itemBatchApprove(companyId);

        } else if (typeCode.equals("CAR")) {
            result = carBatchApprove(companyId);

        } else if (typeCode.equals("ITEM")) {
            result = itemBatchApprove(companyId);
        } else return 0;

        return result;
    }

    /**
     * 차량 예약 일괄 승인
     * 1. companyId
     * 2. typeCode = CAR
     * 3. status = PENDING
     * 4. resourceId - Grouping
     * 5. 날짜 겹치는지 판단
     * 6. 날짜 겹치지 않는 예약은 승인 처리
     * 7. 날짜 겹치지 않는 예약 cnt 반환
     */
    private int carBatchApprove(Long companyId) {

        // 승인 대기 차량 예약 리스트 조회
        List<Reservation> waitingCars = reservationRepository.findWaitingCompanyAndTypeCode(
                companyId,
                ReservationTypeCode.CAR,
                ReservationStatusCode.PENDING
        );

        // 승인 대기 중인 차량 예약이 없으면 0반환
        if (waitingCars.isEmpty()) return 0;

        // resourceId 기준 그룹핑
        Map<Long, List<Reservation>> groups = waitingCars
                .stream()
                .collect(Collectors.groupingBy(
                        r -> r.getResourceId()
                ));

        // 각 resourceId 그룹에서 시간이 겹치지 않는 Reservation 리스트
        List<Long> approveIds = new ArrayList<>();

        for (List<Reservation> group : groups.values()) {

            // 날짜 기준 정렬
            group.sort(Comparator.comparing(Reservation::getReservationDate));

            // 날짜가 겹치는 예약의 id 저장
            // A - B, B - C 이렇게 겹치면 ABBC가 되므로 SET 사용
            Set<Long> conflictIds = new HashSet<>();

            // 모든 쌍 비교
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {

                    if (isCarDateOverlapping(group.get(i), group.get(j))) {
                        conflictIds.add(group.get(i).getId());
                        conflictIds.add(group.get(j).getId());
                    }
                }
            }

            // 겹치지 않은 예약만 승인 리스트에 추가
            for (Reservation r : group) {
                if (!conflictIds.contains(r.getId())) {
                    approveIds.add(r.getId());
                }
            }
        }

        if (!approveIds.isEmpty()) {
            reservationRepository.bulkUpdateStatus(
                    approveIds,
                    getConfirmStatus()
            );
        }

        return approveIds.size();
    }

    // 기타 자원 예약 일괄 승인
    private int itemBatchApprove(Long companyId) {

        // 승인 대기 상태의 자원 리스트 조회
        List<Reservation> waitingItems = reservationRepository.findWaitingCompanyAndTypeCode(
                companyId,
                ReservationTypeCode.ITEM,
                ReservationStatusCode.PENDING
        );

        if (waitingItems.isEmpty()) return 0;

        // resourceId 기준 grouping
        Map<Long, List<Reservation>> groups = waitingItems
                .stream()
                .collect(Collectors.groupingBy(
                        Reservation::getResourceId
                ));

        // 승인할 자원의 id 리스트
        List<Long> approveIds = new ArrayList<>();

        for (List<Reservation> group : groups.values()) {

            // 날짜 + 시작시간 기준 정렬
            group.sort(
                    Comparator
                            .comparing(Reservation::getReservationDate)
                            .thenComparingInt(Reservation::getStartTime)
            );

            // 날짜가 겹치는 예약의 id 저장
            // A - B, B - C 이렇게 겹치면 ABBC가 되므로 SET 사용
            Set<Long> conflictIds = new HashSet<>();

            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {

                    Reservation a = group.get(i);
                    Reservation b = group.get(j);

                    if (isItemTimeOverlapping(a, b)) {
                        conflictIds.add(a.getId());
                        conflictIds.add(b.getId());
                    }
                }
            }

            // 겹치지 않는 예약만 일괄 승인 리스트에 추가
            for (Reservation r : group) {
                if (!conflictIds.contains(r.getId())) {
                    approveIds.add(r.getId());
                }
            }
        }

        if (!approveIds.isEmpty()) {
            reservationRepository.bulkUpdateStatus(
                    approveIds,
                    getConfirmStatus()
            );
        }

        return approveIds.size();
    }

    // 차량 예약 날짜 겹치는지 판단하는 메소드
    private boolean isCarDateOverlapping(Reservation a, Reservation b) {

        LocalDate aStartDate = a.getReservationDate();
        LocalDate aEndDate = a.getEndDate();

        LocalDate bStartDate = b.getReservationDate();
        LocalDate bEndDate = b.getEndDate();

        // A의 종료일이 B의 시작일보다 이전이면 겹치지 않음
        // A의 시작일이 B의 종료일보다 이후면 겹치지 않음
        // 둘 다 true -> 날짜가 겹치는 예약임
        return !aEndDate.isBefore(bStartDate) && !aStartDate.isAfter(bEndDate);
    }

    // 자원 예약 날짜 겹치는지 판단하는 메소드
    private boolean isItemTimeOverlapping(Reservation a, Reservation b) {

        // 날짜가 다르다면 겹치지 않음
        if (!a.getReservationDate().equals(b.getReservationDate())) {
            return false;
        }

        int aStartTime = a.getStartTime();
        int aEndTime = a.getEndTime();

        int bStartTime = b.getStartTime();
        int bEndTime = b.getEndTime();

        return (aStartTime < bEndTime) && (aEndTime > bStartTime);
    }

    // 예약 승인 상태 반환
    private ReservationStatus getConfirmStatus() {
        return reservationStatusRepository.findByStatusCode(ReservationStatusCode.CONFIRM);
    }

    // endregion
    // ---------------------------------------------------------------------------------- //


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

        if (reservation.getTypeCode() == ReservationTypeCode.MEETING) {
            resourceName = meetingroomRepository.findById(reservation.getResourceId()).get().getName();
        } else if (reservation.getTypeCode() == ReservationTypeCode.CAR) {
            resourceName = carRepository.findById(reservation.getResourceId()).get().getName();
        } else if (reservation.getTypeCode() == ReservationTypeCode.ITEM) {
            typeName = itemCategoryRepository.findById(itemCategoryId).get().getName();
            resourceName = itemRepository.findById(reservation.getResourceId()).get().getName();
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
                .endDate(reservation.getEndDate())
                .startTime(reservation.getStartTime())                      // 차량은 - 으로 표시
                .endTime(reservation.getEndTime())                          // 차량은 - 으로 표시
                .reservationReason(reservation.getReservationReason())
                .rejectReason(reservation.getRejectReason())
                .reservationStatusId(reservationStatusId)
                .reservationStatusName(reservationStatusName)
                .build();
    }

}
