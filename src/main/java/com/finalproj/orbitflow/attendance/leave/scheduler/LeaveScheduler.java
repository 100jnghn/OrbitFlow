package com.finalproj.orbitflow.attendance.leave.scheduler;

import com.finalproj.orbitflow.attendance.leave.service.LeaveService;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveScheduler {

    private final CompanyRepository companyRepository;
    private final LeaveService leaveService;


    /**
     * [매년 1월 1일 00:05] 정기 연차 일괄 부여 (회계년도 기준)
     */
    @Scheduled(cron = "0 5 0 1 1 ?")
    public void runAnnualLeaveBatch() {
        int targetYear = LocalDate.now().getYear();
        log.info("{}년도 전사 정기 연차 부여 배치를 시작합니다.", targetYear);

        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            try {
                leaveService.batchGrantAnnualLeave(company.getId(), targetYear);
                log.info("회사 ID: {} - 정기 연차 부여 성공", company.getId());
            } catch (Exception e) {
                log.error("회사 ID: {} - 정기 연차 부여 실패: {}", company.getId(), e.getMessage());
            }
        }
    }


    /**
     * [매일 00:05] 당일 휴가/출장자 근무 상태(Badge) 자동 업데이트
     */
    @Scheduled(cron = "0 5 0 * * *") // 매일 00시 05분에 실행
    public void updateDailyWorkStatus() {
        log.info("당일 근태 기반 근무 상태 업데이트 배치를 시작합니다.");
        try {
            leaveService.updateAllEmployeesWorkStatus(LocalDate.now());
            log.info("근무 상태 업데이트 완료");
        } catch (Exception e) {
            log.error("근무 상태 업데이트 중 오류 발생", e);
        }
    }

    /**
     * [매일 00:10] 당일 attendance 상태 보정
     */
    @Scheduled(cron = "0 10 0 * * *")
    public void normalizeTodayAttendanceStatus() {
        LocalDate today = LocalDate.now();
        log.info("당일 attendance 상태 보정 배치 시작");

        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            try {
                leaveService.normalizeTodayAttendanceStatus(company.getId(), today);
            } catch (Exception e) {
                log.error("회사 {} attendance 보정 실패", company.getId(), e);
            }
        }
    }

}