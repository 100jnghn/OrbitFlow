package com.finalproj.orbitflow.attendance.default_rule.initializer;

import com.finalproj.orbitflow.attendance.default_rule.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.default_rule.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceRuleInitializer
 * @since : 2025. 12. 28. 일요일
 */

@Component
@RequiredArgsConstructor
public class AttendanceRuleInitializer implements ApplicationRunner {
    private final AttendanceRuleRepository ruleRepository; // 근태 규칙 JPA
    private final CompanyRepository companyRepository;     // 회사 정보 JPA

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 1. 시스템에 등록된 모든 회사 조회
        List<Company> companies = companyRepository.findAll();

        for (Company company : companies) {
            // 2. 해당 회사의 규칙이 DB에 이미 존재하는지 확인
            boolean exists = ruleRepository.existsByCompanyId(company.getId());

            if (!exists) {
                // 3. 없으면 9시-6시 기본 규칙 생성하여 저장
                AttendanceRule defaultRule = AttendanceRule.builder()
                        .companyId(company.getId())
                        .name("표준 근무 규칙")
                        .defaultStartTime(LocalTime.of(9, 0))
                        .defaultEndTime(LocalTime.of(18, 0))
                        .lateThresholdMin(10) // 10분 지각 허용 등 세부설정
                        .isDefault(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                ruleRepository.save(defaultRule);
                System.out.println("회사 ID [" + company.getId() + "]에 기본 근태 규칙을 생성했습니다.");
            }
        }
    }
}