package com.finalproj.orbitflow.hr.orgPositionUsage.service;

import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.InvalidStateException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.orgPositionUsage.dto.OrgPositionPolicyUpdateReqDto;
import com.finalproj.orbitflow.hr.orgPositionUsage.dto.OrgPositionUsageResDto;
import com.finalproj.orbitflow.hr.orgPositionUsage.entity.OrgPositionUsage;
import com.finalproj.orbitflow.hr.orgPositionUsage.repository.OrgPositionUsageRepository;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import com.finalproj.orbitflow.hr.positionCategory.repository.PositionCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 조직별 직책 사용 정책 서비스 (화이트리스트)
 *
 * - 정책은 조직 단위로 관리
 * - 레코드 존재 = 허용
 * - 정책 저장은 항상 덮어쓰기 방식
 *
 * @author : seunga03
 * @filename : OrgPositionUsageService
 * @since : 2025-12-23 화요일
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrgPositionUsageService {

    private final OrgPositionUsageRepository repository;
    private final CompanyRepository companyRepository;
    private final OrgRepository orgRepository;
    private final PositionCategoryRepository positionCategoryRepository;

    /* ================= 조회 ================= */
    @Transactional(readOnly = true)
    public List<OrgPositionUsageResDto> findByOrg(Long companyId, Long orgId) {

        Organization org = getActiveOrg(companyId, orgId);

        return repository.findAllUsagesByOrgId(org.getId())
                .stream()
                .map(OrgPositionUsageResDto::from)
                .toList();
    }

    /* ================= 저장 (덮어쓰기) ================= */
    @Transactional
    public void updatePolicy(
            Long companyId,
            OrgPositionPolicyUpdateReqDto request
    ) {
        Company company = getCompany(companyId);
        Organization org = getActiveOrg(companyId, request.getOrgId());

        List<Long> requestedIds = request.getPositionCategoryIds();

        // 중복 검증
        if (requestedIds.size() != requestedIds.stream().distinct().count()) {
            throw new InvalidRequestException("중복된 직책 카테고리가 포함되어 있습니다.");
        }

        // 기존 정책 조회
        List<OrgPositionUsage> existingUsages =
                repository.findByCompany_IdAndOrganization_Id(companyId, org.getId());

        Set<Long> existingIds = existingUsages.stream()
                .map(u -> u.getPositionCategory().getId())
                .collect(Collectors.toSet());

        Set<Long> requestedSet = new HashSet<>(requestedIds);

        // diff 계산
        Set<Long> toAdd = new HashSet<>(requestedSet);
        toAdd.removeAll(existingIds);

        Set<Long> toRemove = new HashSet<>(existingIds);
        toRemove.removeAll(requestedSet);

        // 삭제 (빠진 것만)
        if (!toRemove.isEmpty()) {
            repository.deleteByCompany_IdAndOrganization_IdAndPositionCategory_IdIn(
                    companyId,
                    org.getId(),
                    toRemove.stream().toList()
            );
        }

        // 추가 (새로 들어온 것만)
        for (Long positionCategoryId : toAdd) {

            PositionCategory category =
                    positionCategoryRepository.findById(positionCategoryId)
                            .orElseThrow(() ->
                                    new NotFoundException("직책 카테고리를 찾을 수 없습니다.")
                            );

            // 회사 검증
            if (!category.getCompany().getId().equals(companyId)) {
                throw new ForbiddenException("해당 회사의 직책 카테고리가 아닙니다.");
            }

            // 활성 검증
            if (!Boolean.TRUE.equals(category.getIsActive())) {
                throw new InvalidStateException("비활성 직책 카테고리는 정책에 포함할 수 없습니다.");
            }

            // 멱등성 보호 (동시 요청 대비)
            if (repository.existsByCompanyIdAndOrganizationIdAndPositionCategoryId(
                    companyId, org.getId(), positionCategoryId)) {
                continue;
            }

            repository.save(
                    OrgPositionUsage.create(company, org, category)
            );
        }
    }


    /* ================= 내부 ================= */
    private Company getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("회사를 찾을 수 없습니다."));
    }

    private Organization getActiveOrg(Long companyId, Long orgId) {
        Organization org = orgRepository.findByCompanyIdAndId(companyId, orgId)
                .orElseThrow(() -> new NotFoundException("조직을 찾을 수 없습니다."));

        if (!Boolean.TRUE.equals(org.getIsActive())) {
            throw new ForbiddenException("비활성 조직에는 정책을 설정할 수 없습니다.");
        }
        return org;
    }

}
