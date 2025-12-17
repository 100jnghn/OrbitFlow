package com.finalproj.orbitflow.hr.company.service;

import com.finalproj.orbitflow.hr.company.dto.CompanySignupRequest;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OrganizationRepository organizationRepository;
    private final OrgCategoryRepository orgCategoryRepository;

    public Long signup(CompanySignupRequest request) {

        if (companyRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new IllegalArgumentException("이미 등록된 사업자번호입니다.");
        }

        // 회사 생성
        Company company = Company.create(
                request.getCompanyName(),
                request.getBusinessNumber(),
                request.getAddress(),
                request.getRepresentativeName(),
                request.getRepresentativeContact()
        );
        companyRepository.save(company);

        // 3. 기본 조직 카테고리 생성
        OrgCategory companyCat =
                orgCategoryRepository.save(
                        OrgCategory.create(company.getId(), "회사", 0)
                );
        OrgCategory hqCat =
                orgCategoryRepository.save(
                        OrgCategory.create(company.getId(), "본부", 1)
                );
        OrgCategory deptCat =
                orgCategoryRepository.save(
                        OrgCategory.create(company.getId(), "부서", 2)
                );
        OrgCategory teamCat =
                orgCategoryRepository.save(
                        OrgCategory.create(company.getId(), "팀", 3)
                );

        // 4. 기본 조직 트리 생성
        Organization rootOrg = organizationRepository.save(
                Organization.createRoot(company, companyCat.getId())
        );

        Organization hqOrg = organizationRepository.save(
                Organization.create(
                        company.getId(),
                        hqCat.getId(),
                        rootOrg.getId(),
                        "기본생성본부",
                        1
                )
        );

        Organization deptOrg = organizationRepository.save(
                Organization.create(
                        company.getId(),
                        deptCat.getId(),
                        hqOrg.getId(),
                        "기본생성부",
                        1
                )
        );

        Organization teamOrg = organizationRepository.save(
                Organization.create(
                        company.getId(),
                        teamCat.getId(),
                        deptOrg.getId(),
                        "기본생성팀",
                        1
                )
        );

        // 대표 관리자 생성
        Employee admin = Employee.createAdmin(
                company,
                rootOrg,
                request.getAdminEmail(),
                passwordEncoder.encode(request.getAdminPassword())
        );
        employeeRepository.save(admin);

        return company.getId();
    }
}

