package com.finalproj.orbitflow.hr.position.service;

import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.InvalidStateException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.position.dto.PositionOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.position.dto.PositionReqDto;
import com.finalproj.orbitflow.hr.position.dto.PositionResDto;
import com.finalproj.orbitflow.hr.position.entity.Position;
import com.finalproj.orbitflow.hr.position.repository.PositionRepository;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import com.finalproj.orbitflow.hr.positionCategory.repository.PositionCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionService
 * @since : 2025-12-22 월요일
 */

@Service
@RequiredArgsConstructor
@Transactional
public class PositionService {

    private final PositionRepository positionRepository;
    private final PositionCategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    /* ================= 생성 ================= */
    public Long create(Long companyId, PositionReqDto req) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("회사 없음"));

        PositionCategory category =
                categoryRepository.findById(req.getCategoryId())
                        .orElseThrow(() -> new NotFoundException("직책 카테고리 없음"));

        if (!category.getIsActive()) {
            throw new InvalidStateException("비활성 직책 카테고리에는 직책을 생성할 수 없습니다.");
        }

        Position parent = null;
        if (req.getParentPositionId() != null) {
            parent = get(companyId, req.getParentPositionId());
            if (!parent.getIsActive()) {
                throw new InvalidStateException("비활성 상위 직책은 부모로 지정할 수 없습니다.");
            }
        }

        Integer maxOrder =
                positionRepository.findMaxCompanyActiveOrderIndex(companyId);

        Position position =
                Position.create(
                        company,
                        category,
                        parent,
                        req.getName(),
                        maxOrder == null ? 1 : maxOrder + 1
                );

        return positionRepository.save(position).getId();
    }

    /* ================= 조회 ================= */
    @Transactional(readOnly = true)
    public List<PositionResDto> findAll(Long companyId, boolean includeInactive) {

        List<Position> list =
                includeInactive
                        ? positionRepository
                        .findByCompanyIdOrderByIsActiveDescOrderIndexAscCreatedAtDesc(companyId)
                        : positionRepository
                        .findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(companyId);

        return list.stream()
                .map(PositionResDto::from)
                .toList();
    }

    /* ================= 수정 ================= */
    public void update(Long companyId, Long positionId, PositionReqDto req) {

        Position position = get(companyId, positionId);

        /* ===== category 변경 ===== */
        PositionCategory category =
                categoryRepository.findById(req.getCategoryId())
                        .orElseThrow(() -> new NotFoundException("직책 카테고리 없음"));

        if (!category.getIsActive()) {
            throw new InvalidStateException("비활성 카테고리로 변경할 수 없습니다.");
        }

        /* ===== parent 변경 ===== */
        Position parent = null;
        if (req.getParentPositionId() != null) {
            parent = get(companyId, req.getParentPositionId());
            if (!parent.getIsActive()) {
                throw new InvalidStateException("비활성 상위 직책은 지정할 수 없습니다.");
            }
        }

        boolean wasInactive = !position.getIsActive();
        boolean willActivate = Boolean.TRUE.equals(req.getIsActive());

        if (wasInactive && willActivate) {
            Integer maxOrder =
                    positionRepository.findMaxCompanyActiveOrderIndex(companyId);
            position.updateOrderIndex(maxOrder == null ? 1 : maxOrder + 1);
        }

        if (Boolean.FALSE.equals(req.getIsActive())) {

            boolean hasChild =
                    positionRepository
                            .existsByCompanyIdAndParentPositionIdAndIsActiveTrue(
                                    companyId, positionId
                            );

            if (hasChild) {
                throw new InvalidStateException("하위 직책이 존재하여 비활성화할 수 없습니다.");
            }

            boolean used =
                    employeeRepository
                            .existsByCompanyIdAndPositionId(companyId, positionId);

            if (used) {
                throw new InvalidStateException("해당 직책을 사용하는 사원이 존재합니다.");
            }

            position.updateOrderIndex(null);
        }

        position.update(req.getName(), req.getIsActive());
        position.updateParent(parent);
        position.changeCategory(category); // 아래 엔티티 메서드 추가
    }

    /* ================= 정렬 ================= */
    public void updateOrder(
            Long companyId,
            List<PositionOrderUpdateReqDto.OrderItem> orders
    ) {
        if (orders == null || orders.isEmpty()) {
            throw new InvalidRequestException("순서 정보가 비어 있습니다.");
        }

        long activeCount =
                positionRepository.countByCompanyIdAndIsActiveTrue(companyId);

        if (activeCount != orders.size()) {
            throw new InvalidStateException(
                    "회사 내 활성 직책 전체를 대상으로 정렬해야 합니다."
            );
        }

        // 중복 ID 검증
        Set<Long> uniqueIds = new HashSet<>();


        int temp = -1;
        for (var item : orders) {

            if (!uniqueIds.add(item.getId())) {
                throw new InvalidRequestException("중복된 직책 ID가 존재합니다.");
            }

            Position p = get(companyId, item.getId());

            if (!p.getIsActive()) {
                throw new InvalidStateException("비활성 직책은 정렬할 수 없습니다.");
            }

            p.updateOrderIndex(temp--);
        }

        positionRepository.flush();

        int orderIndex = 1;
        for (var item : orders) {
            Position p = get(companyId, item.getId());
            p.updateOrderIndex(orderIndex++);
        }
    }

    /* ================= 내부 ================= */
    private Position get(Long companyId, Long id) {
        Position p = positionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("직책 없음"));

        if (!p.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 회사의 직책이 아닙니다.");
        }
        return p;
    }
}
