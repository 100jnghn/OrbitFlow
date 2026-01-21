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

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveScheduler
 * @since : 2025. 12. 24. 수요일
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveScheduler {

    private final CompanyRepository companyRepository;
    private final LeaveService leaveService;

    @Scheduled(cron = "0 5 0 1 1 ?")
    public void runAnnualLeaveBatch() {
        int targetYear = LocalDate.now().getYear();

        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            try {
                leaveService.batchGrantAnnualLeave(company.getId(), targetYear);
            } catch (Exception e) {
            }
        }
    }


    @Scheduled(cron = "0 5 0 * * *")
    public void updateDailyWorkStatus() {
        try {
            leaveService.updateAllEmployeesWorkStatus(LocalDate.now());
        } catch (Exception e) {
        }
    }

    @Scheduled(cron = "0 10 0 * * *")
    public void normalizeTodayAttendanceStatus() {
        LocalDate today = LocalDate.now();

        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            try {
                leaveService.normalizeTodayAttendanceStatus(company.getId(), today);
            } catch (Exception e) {
            }
        }
    }

}