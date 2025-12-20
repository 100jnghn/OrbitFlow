package com.finalproj.orbitflow.hr.rank.service;

import com.finalproj.orbitflow.global.exception.BusinessException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.rank.dto.RankCreateReqDto;
import com.finalproj.orbitflow.hr.rank.dto.RankOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.rank.dto.RankResDto;
import com.finalproj.orbitflow.hr.rank.dto.RankUpdateReqDto;
import com.finalproj.orbitflow.hr.rank.entity.HrRank;
import com.finalproj.orbitflow.hr.rank.repository.RankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankService
 * @since : 2025-12-20 토요일
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankService {

    private final RankRepository rankRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    public List<RankResDto> getRanks(Long companyId, String keyword, boolean includeInactive) {

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        List<HrRank> ranks;

        if (hasKeyword) {
            if (includeInactive) {
                ranks = rankRepository.findByCompanyIdAndNameContainingIgnoreCaseOrderByOrderIndexAsc(companyId, keyword.trim());
            } else {
                ranks = rankRepository.findByCompanyIdAndIsActiveTrueAndNameContainingIgnoreCaseOrderByOrderIndexAsc(companyId, keyword.trim());
            }
        } else {
            if (includeInactive) {
                ranks = rankRepository.findByCompanyIdOrderByOrderIndexAsc(companyId);
            } else {
                ranks = rankRepository.findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(companyId);
            }
        }

        // employeeCount 포함
        return ranks.stream()
                .map(rank -> RankResDto.builder()
                        .id(rank.getId())
                        .name(rank.getName())
                        .parentRankId(rank.getParentHrRank() != null ? rank.getParentHrRank().getId() : null)
                        .parentRankName(rank.getParentHrRank() != null ? rank.getParentHrRank().getName() : null)
                        .orderIndex(rank.getOrderIndex())
                        .isActive(rank.getIsActive())
                        .employeeCount(employeeRepository.countByCompanyIdAndRank_Id(companyId, rank.getId()))
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createRank(Long companyId, RankCreateReqDto request) {

        String name = normalizeName(request.getName());
        validateNameNotEmpty(name);

        if (rankRepository.existsByCompanyIdAndName(companyId, name)) {
            throw new BusinessException("이미 존재하는 직급명입니다.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("회사를 찾을 수 없습니다."));

        HrRank parent = null;
        if (request.getParentRankId() != null) {
            parent = getRankInCompanyOrThrow(companyId, request.getParentRankId());
            if (!Boolean.TRUE.equals(parent.getIsActive())) {
                throw new BusinessException("비활성 직급은 상위 직급으로 지정할 수 없습니다.");
            }
        }

        Integer nextOrderIndex = calcNextOrderIndex(companyId);

        HrRank saved = rankRepository.save(HrRank.create(company, parent, name, nextOrderIndex));
        return saved.getId();
    }

    @Transactional
    public void updateRank(Long companyId, Long rankId, RankUpdateReqDto request) {

        HrRank rank = getRankInCompanyOrThrow(companyId, rankId);

        String name = normalizeName(request.getName());
        validateNameNotEmpty(name);

        if (rankRepository.existsByCompanyIdAndNameAndIdNot(companyId, name, rankId)) {
            throw new BusinessException("이미 존재하는 직급명입니다.");
        }

        HrRank parent = null;
        if (request.getParentRankId() != null) {
            if (Objects.equals(request.getParentRankId(), rankId)) {
                throw new BusinessException("자기 자신을 상위 직급으로 설정할 수 없습니다.");
            }
            parent = getRankInCompanyOrThrow(companyId, request.getParentRankId());

            if (!Boolean.TRUE.equals(parent.getIsActive())) {
                throw new BusinessException("비활성 직급은 상위 직급으로 지정할 수 없습니다.");
            }
        }

        // 비활성화 정책: 부여 인원 1명 이상이면 비활성 불가
        if (Boolean.FALSE.equals(request.getIsActive())) {
            long employeeCount = employeeRepository.countByCompanyIdAndRank_Id(companyId, rankId);
            if (employeeCount > 0) {
                throw new BusinessException("부여 인원이 있는 직급은 비활성화할 수 없습니다. (사용자에서 변경 후 가능)");
            }
        }

        Boolean isActive = request.getIsActive() != null
                ? request.getIsActive()
                : rank.getIsActive();

        rank.update(name, parent, isActive);
    }

    @Transactional
    public void updateOrder(Long companyId, List<RankOrderUpdateReqDto> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new BusinessException("변경할 순서 정보가 없습니다.");
        }

        // 유효성: orderIndex 중복/누락은 프론트에서 보통 방지하지만, 최소 검증
        requests.sort(Comparator.comparing(RankOrderUpdateReqDto::getOrderIndex));

        for (RankOrderUpdateReqDto req : requests) {
            HrRank rank = getRankInCompanyOrThrow(companyId, req.getRankId());
            rank.changeOrderIndex(req.getOrderIndex());
        }
    }

    private HrRank getRankInCompanyOrThrow(Long companyId, Long rankId) {
        HrRank rank = rankRepository.findById(rankId)
                .orElseThrow(() -> new BusinessException("직급을 찾을 수 없습니다."));

        if (!Objects.equals(rank.getCompany().getId(), companyId)) {
            throw new BusinessException("해당 회사의 직급이 아닙니다.");
        }
        return rank;
    }

    private Integer calcNextOrderIndex(Long companyId) {
        List<HrRank> ranks = rankRepository.findByCompanyIdOrderByOrderIndexAsc(companyId);
        if (ranks.isEmpty()) return 1;
        int max = ranks.stream()
                .map(HrRank::getOrderIndex)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        return max + 1;
    }

    private String normalizeName(String raw) {
        return raw == null ? null : raw.trim();
    }

    private void validateNameNotEmpty(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("직급명은 필수입니다.");
        }
        if (name.length() > 50) {
            throw new BusinessException("직급명은 50자 이하여야 합니다.");
        }
    }
}
