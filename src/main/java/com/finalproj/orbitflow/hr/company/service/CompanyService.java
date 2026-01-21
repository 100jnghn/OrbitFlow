package com.finalproj.orbitflow.hr.company.service;

import com.finalproj.orbitflow.board.boardcategory.service.OrganizationBoardCategorySyncService;
import com.finalproj.orbitflow.global.exception.BusinessException;
import com.finalproj.orbitflow.hr.company.dto.CompanySignupReqDto;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.event.CompanyCreatedEvent;
import com.finalproj.orbitflow.hr.company.external.BsnClient;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
import com.finalproj.orbitflow.hr.orgPositionUsage.entity.OrgPositionUsage;
import com.finalproj.orbitflow.hr.orgPositionUsage.repository.OrgPositionUsageRepository;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import com.finalproj.orbitflow.hr.positionCategory.repository.PositionCategoryRepository;
import com.finalproj.orbitflow.hr.rank.entity.HrRank;
import com.finalproj.orbitflow.hr.rank.repository.RankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;

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
    private final ApplicationEventPublisher eventPublisher;
    private final OrgPositionUsageRepository orgPositionUsageRepository;
    private final RankRepository rankRepository;
    private final OrganizationBoardCategorySyncService organizationBoardCategorySyncService;


    @Value("${business.validation.strict}")
    private boolean strictValidation;


    public Long signup(CompanySignupReqDto request) {

        // 회사명 정규화
        String normalizedCompanyName = normalizeCompanyName(
                request.getCompanyName()
        );

        // 회사명 중복 체크 (정규화된 값 기준)
        if (companyRepository.existsByName(normalizedCompanyName)) {
            throw new BusinessException("이미 사용 중인 회사명입니다.");
        }

        // 사업자번호 검증 (trim 권장)
        String businessNumber = request.getBusinessNumber().trim();
        validateBusinessNumber(businessNumber);

        // 이메일 중복 체크 (trim 권장)
        String adminEmail = request.getAdminEmail().trim();
        if (employeeRepository.existsByEmail(adminEmail)) {
            throw new BusinessException("이미 사용 중인 이메일입니다.");
        }

        // 회사 생성 (정규화된 회사명 저장)
        Company company = Company.create(
                normalizedCompanyName,
                businessNumber,
                request.getAddress().trim(),
                request.getRepresentativeName().trim(),
                request.getRepresentativeContact().trim()
        );
        companyRepository.save(company);

        // 기본 조직 카테고리 생성
        OrgCategory companyCat = orgCategoryRepository.save(
                OrgCategory.create(company.getId(), "회사", null, true)
        );
        OrgCategory hqCat = orgCategoryRepository.save(
                OrgCategory.create(company.getId(), "본부", 1, false)
        );
        OrgCategory deptCat = orgCategoryRepository.save(
                OrgCategory.create(company.getId(), "부서", 2, false)
        );
        OrgCategory teamCat = orgCategoryRepository.save(
                OrgCategory.create(company.getId(), "팀", 3, false)
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

        // ================= 조직 게시판 자동 생성 =================
        organizationBoardCategorySyncService.createIfAbsent(
                company.getId(), rootOrg.getId(), rootOrg.getName()
        );

        organizationBoardCategorySyncService.createIfAbsent(
                company.getId(), hqOrg.getId(), hqOrg.getName()
        );

        organizationBoardCategorySyncService.createIfAbsent(
                company.getId(), deptOrg.getId(), deptOrg.getName()
        );

        organizationBoardCategorySyncService.createIfAbsent(
                company.getId(), teamOrg.getId(), teamOrg.getName()
        );


        // 직급
        seedDefaultRanks(company);

        // 직책 카테고리
        seedDefaultPositionCategories(company, companyCat, hqCat, deptCat, teamCat);

        // 조직-직책 정책
        seedDefaultOrgPositionUsage(
                company, rootOrg, hqOrg, deptOrg, teamOrg,
                companyCat, hqCat, deptCat, teamCat
        );




        // 대표 관리자 생성
        Employee admin = Employee.createAdmin(
                company,
                rootOrg,
                request.getAdminEmail(),
                passwordEncoder.encode(request.getAdminPassword())
        );
        employeeRepository.save(admin);


        // 모든 기본 데이터 생성 완료 후
        eventPublisher.publishEvent(
                new CompanyCreatedEvent(company.getId())
        );


        return company.getId();
    }

    /**
     * 이메일 중복 여부 확인
     */
    public boolean isEmailAvailable(String email) {
        return !employeeRepository.existsByEmail(email);
    }


    /**
     * 사업자번호 중복 여부만 확인 (프론트용)
     */
    @Transactional(readOnly = true)
    public void checkBusinessNumberAvailable(String businessNumber) {
        validateBusinessNumber(businessNumber);
    }

    /**
     * 사업자번호 유효성 검증 (공통)
     * - 중복
     * - 외부 API 상태
     */
    private void validateBusinessNumber(String businessNumber) {

        if (companyRepository.existsByBusinessNumber(businessNumber)) {
            throw new BusinessException("이미 등록된 사업자번호입니다.");
        }

        String status;
        try {
            status = bsnClient.getBusinessStatus(businessNumber);
        } catch (Exception e) {
            throw new BusinessException("사업자번호 외부 검증에 실패했습니다.");
        }

        if (strictValidation) {
            if (!"계속사업자".equals(status)) {
                throw new BusinessException("계속사업자만 가입할 수 있습니다.");
            }
        } else {
            if (!"계속사업자".equals(status)
                    && !"휴업자".equals(status)
                    && !"폐업자".equals(status)) {
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
        PositionCategory ceo = positionCategoryRepository.save(
                PositionCategory.create(company, companyCat, null, "사장", 1, true)
        );

        PositionCategory hqHead = positionCategoryRepository.save(
                PositionCategory.create(company, hqCat, ceo, "본부장", 2, true)
        );

        PositionCategory deptHead = positionCategoryRepository.save(
                PositionCategory.create(company, deptCat, hqHead, "부장", 3, true)
        );

        PositionCategory teamLead = positionCategoryRepository.save(
                PositionCategory.create(company, teamCat, deptHead, "팀장", 4, true)
        );

        positionCategoryRepository.save(
                PositionCategory.create(company, teamCat, teamLead, "팀원", 5, false)
        );
    }



    @Transactional(readOnly = true)
    public Company findById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 회사입니다."));
    }

    private void seedDefaultRanks(Company company) {

        HrRank ceo = rankRepository.save(
                HrRank.create(company, null, "사장", 1)
        );

        HrRank director = rankRepository.save(
                HrRank.create(company, ceo, "부장", 2)
        );

        HrRank deputy = rankRepository.save(
                HrRank.create(company, director, "차장", 3)
        );

        HrRank manager = rankRepository.save(
                HrRank.create(company, deputy, "과장", 4)
        );

        HrRank assistant = rankRepository.save(
                HrRank.create(company, manager, "대리", 5)
        );

        rankRepository.save(
                HrRank.create(company, assistant, "사원", 6)
        );
    }




    private void seedDefaultOrgPositionUsage(
            Company company,
            Organization rootOrg,
            Organization hqOrg,
            Organization deptOrg,
            Organization teamOrg,
            OrgCategory companyCat,
            OrgCategory hqCat,
            OrgCategory deptCat,
            OrgCategory teamCat
    ) {
        // 회사
        linkAll(company, rootOrg, companyCat);

        // 본부
        linkAll(company, hqOrg, hqCat);

        // 부서
        linkAll(company, deptOrg, deptCat);

        // 팀
        linkAll(company, teamOrg, teamCat);
    }

    private void linkAll(
            Company company,
            Organization org,
            OrgCategory category
    ) {
        List<PositionCategory> positions =
                positionCategoryRepository.findByCompany_IdAndOrgCategory_IdAndIsActiveTrue(
                        company.getId(),
                        category.getId()
                );

        positions.forEach(pc ->
                orgPositionUsageRepository.save(
                        OrgPositionUsage.create(company, org, pc)
                )
        );
    }

    @Transactional(readOnly = true)
    public void checkCompanyNameAvailable(String name) {
        String normalizedName = normalizeCompanyName(name);

        if (companyRepository.existsByName(normalizedName)) {
            throw new BusinessException("이미 사용 중인 회사명입니다.");
        }
    }



    private String normalizeCompanyName(String name) {
        if (name == null) {
            return null;
        }

        return Normalizer.normalize(name, Normalizer.Form.NFKC)
                .trim()
                .replaceAll("\\s+", " ");
    }

}

