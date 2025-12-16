package com.finalproj.orbitflow.hr.company.service;

import com.finalproj.orbitflow.hr.company.dto.CompanySignupRequest;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CompanyService
 * @since : 2025-12-16 화요일
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public Long signup(CompanySignupRequest request) {

        if (companyRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new IllegalArgumentException("이미 등록된 사업자번호입니다.");
        }

        Company company = Company.create(
                request.getCompanyName(),
                request.getBusinessNumber(),
                request.getAddress(),
                request.getRepresentativeName(),
                request.getRepresentativeContact()
        );
        companyRepository.save(company);

        Employee admin = Employee.createAdmin(
                company,
                request.getAdminEmail(),
                passwordEncoder.encode(request.getAdminPassword())
        );
        employeeRepository.save(admin);

        return company.getId();
    }
}

