package com.finalproj.orbitflow.attendance.rule.listener;

import com.finalproj.orbitflow.attendance.rule.service.AttendanceRuleService;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.event.CompanyCreatedEvent;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RuleEventListener
 * @since : 2026-01-11 일요일
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceRuleEventListener {

    private final CompanyRepository companyRepository;
    private final AttendanceRuleService attendanceRuleService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleCompanyCreated(CompanyCreatedEvent event) {

        Company company = companyRepository.findById(event.getCompanyId())
                .orElse(null);

        if (company == null) {
            log.warn("[AttendanceRule] 회사 없음 - id={}", event.getCompanyId());
            return;
        }

        try {
            attendanceRuleService.createDefaultAttendanceRule(company);
        } catch (Exception e) {
            log.error(
                    "[AttendanceRule] 기본 근태 규칙 생성 실패 - companyId={}",
                    company.getId(),
                    e
            );
        }
    }
}
