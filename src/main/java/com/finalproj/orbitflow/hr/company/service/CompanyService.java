package com.finalproj.orbitflow.hr.company.service;

import com.finalproj.orbitflow.global.exception.BusinessException;
import com.finalproj.orbitflow.hr.company.dto.CompanySignupReqDto;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.external.BsnClient;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import com.finalproj.orbitflow.hr.positionCategory.repository.PositionCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final OrgRepository orgRepository;
    private final OrgCategoryRepository orgCategoryRepository;
    private final BsnClient bsnClient;
    private final PositionCategoryRepository positionCategoryRepository;

    @Value("${business.validation.strict}")
    private boolean strictValidation;


    public Long signup(CompanySignupReqDto request) {

        // 사업자번호 검증
        validateBusinessNumber(request.getBusinessNumber());

        // 대표 관리자 이메일 중복 체크
        if (employeeRepository.existsByEmail(request.getAdminEmail())) {
            throw new BusinessException("이미 사용 중인 이메일입니다.");
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

        // 기본 조직 카테고리 생성
        OrgCategory companyCat = orgCategoryRepository.save(
                OrgCategory.create(company.getId(), "회사", 1)
        );
        OrgCategory hqCat = orgCategoryRepository.save(
                OrgCategory.create(company.getId(), "본부", 2)
        );
        OrgCategory deptCat = orgCategoryRepository.save(
                OrgCategory.create(company.getId(), "부서", 3)
        );
        OrgCategory teamCat = orgCategoryRepository.save(
                OrgCategory.create(company.getId(), "팀", 4)
        );

        // 기본 조직 트리 생성
        Organization rootOrg = orgRepository.save(
                Organization.createRoot(company, companyCat.getId())
        );

        Organization hqOrg = orgRepository.save(
                Organization.create(
                        company.getId(),
                        hqCat.getId(),
                        rootOrg.getId(),
                        "기본생성본부",
                        1
                )
        );

        Organization deptOrg = orgRepository.save(
                Organization.create(
                        company.getId(),
                        deptCat.getId(),
                        hqOrg.getId(),
                        "기본생성부",
                        1
                )
        );

        Organization teamOrg = orgRepository.save(
                Organization.create(
                        company.getId(),
                        teamCat.getId(),
                        deptOrg.getId(),
                        "기본생성팀",
                        1
                )
        );

        // 기본 직책 카테고리 생성 (회사 전역 기준)
        seedDefaultPositionCategories(
                company,
                companyCat,
                hqCat,
                deptCat,
                teamCat
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

    /**
     * 이메일 중복 여부 확인
     */
    public boolean isEmailAvailable(String email) {
        return !employeeRepository.existsByEmail(email);
    }

    /**
     * 사업자 번호 검증
     */
    public void validateBusinessNumber(String businessNumber) {

        // DB 중복 체크 (무조건)
        if (companyRepository.existsByBusinessNumber(businessNumber)) {
            throw new BusinessException("이미 등록된 사업자번호입니다.");
        }

        // 외부 API 상태 조회
        String status;
        try {
            status = bsnClient.getBusinessStatus(businessNumber);
        } catch (Exception e) {
            throw new BusinessException("사업자번호 외부 검증에 실패했습니다.");
        }

        // strict 정책 분기
        if (strictValidation) { // 운영
            if (!"계속사업자".equals(status)) {
                throw new BusinessException("계속사업자만 가입할 수 있습니다.");
            }
        } else { // 개발/시연
            if (!"계속사업자".equals(status)
                    && !"휴업자".equals(status)
                    && !"폐업자".equals(status)
            ) {
                throw new BusinessException("유효하지 않은 사업자번호입니다.");
            }
        }
    }

    // 기본 직책 카테고리 생성 (회사 전역 기준)
    private void seedDefaultPositionCategories(
            Company company,
            OrgCategory companyCat,
            OrgCategory hqCat,
            OrgCategory deptCat,
            OrgCategory teamCat
    ) {
        positionCategoryRepository.save(
                PositionCategory.create(company, companyCat, null, "사장", 1, true)
        );
        positionCategoryRepository.save(
                PositionCategory.create(company, hqCat, null, "본부장", 2, true)
        );
        positionCategoryRepository.save(
                PositionCategory.create(company, deptCat, null, "부장", 3, true)
        );
        positionCategoryRepository.save(
                PositionCategory.create(company, teamCat, null, "팀장", 4, true)
        );
        positionCategoryRepository.save(
                PositionCategory.create(company, teamCat, null, "팀원", 5, false)
        );
    }

}

