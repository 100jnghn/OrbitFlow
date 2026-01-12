package com.finalproj.orbitflow.hr.rank.service;

import com.finalproj.orbitflow.global.exception.BusinessException;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEventType;
import com.finalproj.orbitflow.hr.logAudit.service.AuditLogService;
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
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankService
 * @since : 2025-12-20 토요일
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RankService {

    private final RankRepository rankRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    /* ================= 목록 ================= */
    @Transactional(readOnly = true)
    public List<RankResDto> getRanks(Long companyId, String keyword, boolean includeInactive) {

        List<HrRank> ranks;

        if (includeInactive) {
            ranks = rankRepository
                    .findByCompanyIdOrderByIsActiveDescOrderIndexAscCreatedAtDesc(companyId);
        } else {
            ranks = (keyword == null || keyword.isBlank())
                    ? rankRepository.findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(companyId)
                    : rankRepository
                    .findByCompanyIdAndIsActiveTrueAndNameContainingIgnoreCaseOrderByOrderIndexAsc(
                            companyId, keyword.trim());
        }

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

    /* ================= 생성 ================= */
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

        Integer max = rankRepository.findMaxActiveOrderIndex(companyId);
        int nextOrder = (max == null) ? 1 : max + 1;

        HrRank rank = HrRank.create(company, parent, name, nextOrder);
        HrRank saved = rankRepository.save(rank);

        Employee actor = employeeRepository.findById(
                SecurityUtils.getEmployeeId()
        ).orElseThrow(() -> new IllegalStateException("행위자 사원 정보 없음"));

        Map<String, Object> after = new java.util.HashMap<>();
        after.put("name", saved.getName());
        after.put("parentRankId",
                saved.getParentHrRank() != null
                        ? saved.getParentHrRank().getId()
                        : null
        );
        after.put("orderIndex", saved.getOrderIndex());

        auditLogService.log(
                company,
                actor,
                AuditEntityType.HR_RANK,
                saved.getId(),
                AuditEventType.CREATE,
                null,
                after
        );

        return saved.getId();

    }

    /* ================= 수정 ================= */
    public void updateRank(Long companyId, Long rankId, RankUpdateReqDto req) {

        Employee actor = employeeRepository.findById(
                SecurityUtils.getEmployeeId()
        ).orElseThrow(() -> new IllegalStateException("행위자 사원 정보 없음"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("회사를 찾을 수 없습니다."));

        HrRank rank = getRank(companyId, rankId);


        String oldName = rank.getName();
        Long oldParentId = rank.getParentHrRank() != null
                ? rank.getParentHrRank().getId()
                : null;


        String name = req.getName().trim();
        if (rankRepository.existsByCompanyIdAndNameAndIdNot(companyId, name, rankId)) {
            throw new BusinessException("이미 존재하는 직급명입니다.");
        }

        HrRank parent = null;
        if (req.getParentRankId() != null) {
            if (req.getParentRankId().equals(rankId)) {
                throw new BusinessException("자기 자신을 상위 직급으로 지정할 수 없습니다.");
            }
            parent = getRank(companyId, req.getParentRankId());
        }

        // ===== 활성/비활성 =====
        if (req.getIsActive() != null) {

            // 비활성화
            if (!req.getIsActive()) {
                long count = employeeRepository.countByCompanyIdAndRank_Id(companyId, rankId);
                if (count > 0) {
                    throw new BusinessException("부여 인원이 있는 직급은 비활성화할 수 없습니다.");
                }
                rank.deactivate();

                auditLogService.log(
                        company,
                        actor,
                        AuditEntityType.HR_RANK,
                        rank.getId(),
                        AuditEventType.DEACTIVATE,
                        Map.of("isActive", true),
                        Map.of("isActive", false)
                );

            }

            // 재활성화
            if (req.getIsActive() && !rank.getIsActive()) {
                Integer max = rankRepository.findMaxActiveOrderIndex(companyId);
                rank.activate(max == null ? 1 : max + 1);

                auditLogService.log(
                        company,
                        actor,
                        AuditEntityType.HR_RANK,
                        rank.getId(),
                        AuditEventType.ACTIVATE,
                        Map.of("isActive", false),
                        Map.of("isActive", true)
                );

            }
        }


        Long newParentId = parent != null ? parent.getId() : null;

        boolean nameChanged = !oldName.equals(name);
        boolean parentChanged = !java.util.Objects.equals(oldParentId, newParentId);

        if (nameChanged || parentChanged) {
            // 이름/상위 직급만 변경
            rank.update(name, parent);

            Map<String, Object> before = new java.util.HashMap<>();
            before.put("name", oldName);
            before.put("parentRankId", oldParentId);

            Map<String, Object> after = new java.util.HashMap<>();
            after.put("name", name);
            after.put("parentRankId", newParentId);

            auditLogService.log(
                    company,
                    actor,
                    AuditEntityType.HR_RANK,
                    rank.getId(),
                    AuditEventType.UPDATE,
                    before,
                    after
            );
        }


    }


    /* ================= 순서 ================= */
    public void updateOrder(Long companyId, List<RankOrderUpdateReqDto.OrderItem> orders) {

        long activeCount = rankRepository.countByCompanyIdAndIsActiveTrue(companyId);
        if (activeCount != orders.size()) {
            throw new BusinessException("정렬 대상 개수가 일치하지 않습니다.");
        }

        int temp = -1;
        for (var item : orders) {
            getRank(companyId, item.getId()).changeOrderIndex(temp--);
        }
        rankRepository.flush();

        int index = 1;
        for (var item : orders) {
            HrRank rank = getRank(companyId, item.getId());
            rank.changeOrderIndex(index++);
        }
    }

    private HrRank getRank(Long companyId, Long id) {
        HrRank rank = rankRepository.findById(id)
                .orElseThrow(() -> new BusinessException("직급을 찾을 수 없습니다."));
        if (!rank.getCompany().getId().equals(companyId)) {
            throw new BusinessException("해당 회사의 직급이 아닙니다.");
        }
        return rank;
    }
}
