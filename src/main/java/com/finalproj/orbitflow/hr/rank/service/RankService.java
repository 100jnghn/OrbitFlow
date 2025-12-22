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

import java.util.List;

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

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        List<HrRank> ranks;

        if (hasKeyword) {
            ranks = includeInactive
                    ? rankRepository.findByCompanyIdAndNameContainingIgnoreCaseOrderByOrderIndexAsc(companyId, keyword.trim())
                    : rankRepository.findByCompanyIdAndIsActiveTrueAndNameContainingIgnoreCaseOrderByOrderIndexAsc(companyId, keyword.trim());
        } else {
            ranks = includeInactive
                    ? rankRepository.findByCompanyIdOrderByOrderIndexAsc(companyId)
                    : rankRepository.findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(companyId);
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
                        .build())
                .toList();
    }

    @Transactional
    public Long createRank(Long companyId, RankCreateReqDto req) {

        String name = req.getName().trim();

        if (rankRepository.existsByCompanyIdAndName(companyId, name)) {
            throw new BusinessException("이미 존재하는 직급명입니다.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("회사를 찾을 수 없습니다."));

        HrRank parent = null;
        if (req.getParentRankId() != null) {
            parent = getRank(companyId, req.getParentRankId());
            if (!parent.getIsActive()) {
                throw new BusinessException("비활성 직급은 상위 직급으로 지정할 수 없습니다.");
            }
        }

        Integer orderIndex = calcNextOrder(companyId);
        HrRank saved = rankRepository.save(HrRank.create(company, parent, name, orderIndex));
        return saved.getId();
    }

    @Transactional
    public void updateRank(Long companyId, Long rankId, RankUpdateReqDto req) {

        HrRank rank = getRank(companyId, rankId);

        String name = req.getName().trim();
        if (rankRepository.existsByCompanyIdAndNameAndIdNot(companyId, name, rankId)) {
            throw new BusinessException("이미 존재하는 직급명입니다.");
        }

        HrRank parent = null;
        if (req.getParentRankId() != null) {
            if (req.getParentRankId().equals(rankId)) {
                throw new BusinessException("자기 자신을 상위 직급으로 설정할 수 없습니다.");
            }
            parent = getRank(companyId, req.getParentRankId());
            if (!parent.getIsActive()) {
                throw new BusinessException("비활성 직급은 상위 직급으로 지정할 수 없습니다.");
            }
        }

        if (Boolean.FALSE.equals(req.getIsActive())) {
            long count = employeeRepository.countByCompanyIdAndRank_Id(companyId, rankId);
            if (count > 0) {
                throw new BusinessException("부여 인원이 있는 직급은 비활성화할 수 없습니다.");
            }
        }

        rank.update(name, parent,
                req.getIsActive() != null ? req.getIsActive() : rank.getIsActive());
    }


    @Transactional
    public void updateOrder(Long companyId, List<RankOrderUpdateReqDto.OrderItem> orders) {

        if (orders == null || orders.isEmpty()) {
            throw new BusinessException("순서 정보가 비어 있습니다.");
        }

        long distinctCount = orders.stream()
                .map(RankOrderUpdateReqDto.OrderItem::getId)
                .distinct()
                .count();

        if (distinctCount != orders.size()) {
            throw new BusinessException("중복된 직급 ID가 존재합니다.");
        }

        long activeCount = rankRepository.countByCompanyIdAndIsActiveTrue(companyId);
        if (activeCount != orders.size()) {
            throw new BusinessException("정렬 대상 개수가 일치하지 않습니다.");
        }

        // 1단계: 임시 orderIndex
        int temp = -1;
        for (var item : orders) {
            HrRank rank = getRank(companyId, item.getId());
            if (!rank.getIsActive()) {
                throw new BusinessException("비활성 직급은 순서 변경 대상이 될 수 없습니다.");
            }
            rank.changeOrderIndex(temp--);
        }

        rankRepository.flush();

        // 2단계: 최종 orderIndex
        int index = 1;
        for (var item : orders) {
            HrRank rank = getRank(companyId, item.getId());
            rank.changeOrderIndex(index++);
        }
    }



    private HrRank getRank(Long companyId, Long rankId) {
        HrRank rank = rankRepository.findById(rankId)
                .orElseThrow(() -> new BusinessException("직급을 찾을 수 없습니다."));
        if (!rank.getCompany().getId().equals(companyId)) {
            throw new BusinessException("해당 회사의 직급이 아닙니다.");
        }
        return rank;
    }

    private Integer calcNextOrder(Long companyId) {
        return rankRepository.findByCompanyIdOrderByOrderIndexAsc(companyId)
                .stream()
                .map(HrRank::getOrderIndex)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
