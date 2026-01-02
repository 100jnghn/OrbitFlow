package com.finalproj.orbitflow.schedule.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.schedule.dto.ScheduleReqDto;
import com.finalproj.orbitflow.schedule.dto.ScheduleResDto;
import com.finalproj.orbitflow.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleController
 * @since : 2025-12-27 오후 4:28 토요일
 */
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 관리자 - 전사 일정 조회
     * <p>
     * isCompany : TRUE / isPersonal : FALSE / companyId : NOT NULL / orgCategoryId : NOT NULL / orgId : NULL
     * <p>
     * status ['RELEASE', 'HOLD', 'DELETED', 'ETC']
     * showPast [true, false]
     */
    @GetMapping("/schedules/company")
    public ResponseEntity<ResponseDto> getCompanySchedules(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam int year,
            @RequestParam int month
    ) {
        Long companyId = user.getCompanyId();
        List<ScheduleResDto> schedules = scheduleService.getCompanySchedules(companyId, status, year, month);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "전사 일정 조회 성공", schedules)
        );
    }

    /**
     * 사용자 - 전사 일정 조회
     * isCompany = TRUE, isPersonal = FALSE 전체 조회
     */
    @GetMapping("/schedules/user-company")
    public ResponseEntity<ResponseDto> getUserCompanySchedule(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "false") boolean isWeekly
    ) {
        log.info("사용자 전사 일정");
        Long companyId = user.getCompanyId();
        List<ScheduleResDto> schedules = scheduleService.getUserCompanySchedule(companyId, year, month, isWeekly);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "일정 조회 성공", schedules)
        );
    }

    /**
     * 사용자 + 관리자 - 일정 세부 조회
     */
    @GetMapping("/schedules/schedule/{scheduleId}")
    public ResponseEntity<ResponseDto> getSchedule(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long scheduleId
    ) {
        Long companyId = user.getCompanyId();

        ScheduleResDto schedules = scheduleService.getSchedule(companyId, scheduleId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "일정 조회 성공", schedules)
        );
    }

    /**
     * 사용자 - 조직 일정 검색
     * 일정 캘린더에서 사용
     * 조직 일정 검색에 사용
     * 월 단위 검색, 주 단위 검색
     */
    @GetMapping("/schedules/organizations")
    public ResponseEntity<ResponseDto> getMonthlySchedulesByOrganizations(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam List<Long> orgIds,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "false") boolean isWeekly
    ) {
        Long companyId = user.getCompanyId();
        List<ScheduleResDto> schedules = scheduleService.getOrganizationSchedules(companyId, year, month, orgIds, isWeekly);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "조직 일정 조회 성공", schedules)
        );
    }

    @GetMapping("/schedules/organizations/schedule")
    public ResponseEntity<ResponseDto> getDateOrganizationSchedules(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam List<Long> orgIds,
            @RequestParam LocalDate date
    ) {
        Long companyId = user.getCompanyId();
        List<ScheduleResDto> schedules = scheduleService.getDateOrganizationSchedules(companyId, orgIds, date);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "조직 일정 검색 성공", schedules)
        );
    }

    /**
     * 사용자 - 진짜 개인 일정 조회
     * 월 단위 조회, 주 단위 검색
     */
    @GetMapping("/schedules/personal")
    public ResponseEntity<ResponseDto> getEmployeeSchedules(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "false") boolean isWeekly
    ) {
        Long companyId = user.getCompanyId();
        Long employeeId = user.getEmployeeId();

        List<ScheduleResDto> schedules = scheduleService.getEmployeeSchedules(companyId, employeeId, year, month, isWeekly);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "개인 일정 조회 성공", schedules)
        );
    }

    /**
     * 사용자 - 휴가, 출장 등의 [개인 + 전사] 일정 조회
     * isCompany TRUE / isPersonal TRUE / orgCategoryIs NULL / orgId NOT NULL
     */
    @GetMapping("/schedules/company-employee")
    public ResponseEntity<ResponseDto> getCompanyEmployeeSchedules(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "false") boolean isWeekly
    ) {
        Long companyId = user.getCompanyId();
        Long employeeId = user.getEmployeeId();

        List<ScheduleResDto> schedules = scheduleService.getCompanyEmployeeSchedules(companyId, employeeId, year, month, isWeekly);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "일정 조회 성공", schedules)
        );
    }


    /**
     * 사용자 - 일 단위 일정 조회
     * front page의 캘린더에서 날짜를 선택해서 호출
     * 해당 날짜에 해당하는 전사, 개인, 조직 일정 모두 조회
     */
    @GetMapping("/schedules/schedule")
    public ResponseEntity<ResponseDto> getDateSchedules(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam LocalDate date
    ) {
        Long companyId = user.getCompanyId();
        Long employeeId = user.getEmployeeId();
        Long orgId = user.getOrganizationId();      // 사용자 소속 조직

        List<ScheduleResDto> schedules = scheduleService.getDateSchedules(
                companyId,
                employeeId,
                orgId,
                date
        );

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, date + " 일정 조회 성공", schedules)
        );
    }

    /**
     * 사용자 + 관리자 - 일정 하나 삭제
     * 'HOLD' status가 존재하므로 Hard Delete 구현
     */
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<ResponseDto> deleteSchedule(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable("scheduleId") Long scheduleId
    ) {
        Long companyId = user.getCompanyId();

        scheduleService.deleteSchedule(companyId, scheduleId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "일정 삭제 성공", null)
        );
    }

    /**
     * 관리자 - 전사 일정 하나 수정
     */
    @PatchMapping("/admin/schedules/{scheduleId}")
    public ResponseEntity<ResponseDto> updateSchedule(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody ScheduleReqDto scheduleReqDto
    ) {
        Long companyId = user.getCompanyId();
        scheduleService.updateSchedule(companyId, scheduleId, scheduleReqDto);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "전사 일정 수정 성공", null)
        );
    }

    /**
     * 사용자 - 일정 등록
     *
     * -- 전사 일정 --
     * 관리자 - isCompany : TRUE / isPersonal : FALSE / companyId : NOT NULL / orgCategoryId : NOT NULL / orgId : NULL
     *
     * -- 개인 일정 + 전사 일정 --
     * 사용자 - isCompany : TRUE / isPersonal : TRUE / companyId : NOT NULL / orgCategoryId : NOT NULL / orgId : NOT NULL
     *
     * -- 개인 일정 --
     * 사용자 - isCompany : FALSE / isPersonal : TRUE / companyId : NOT NULL / orgCategoryId : NULL / orgId : NULL
     *
     * -- 조직 일정 --
     * 사용자 - isCompany : FALSE / isPersonal : FALSE / companyId : NOT NULL / orgCategoryId : NOT NULL / orgId : NOT NULL
     *
     * 사용자의 예약의 status는 'RELEASE' OR 삭제
     * 관리자의 예약의 status는 'RELEASE' OR 'HOLD' OR 삭제
     */
    @PostMapping("/schedules")
    public ResponseEntity<ResponseDto> insertSchedule(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody ScheduleReqDto scheduleReqDto
    ) {
        Long companyId = user.getCompanyId();
        Long employeeId = user.getEmployeeId();

        // isCompany 여부는 front에서 dto에 포함시켜 전달함

        scheduleService.insertSchedule(companyId, employeeId, scheduleReqDto);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "일정 등록 성공", null)
        );
    }
}
