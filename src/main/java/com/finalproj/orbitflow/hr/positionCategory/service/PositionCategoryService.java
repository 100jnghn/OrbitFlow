package com.finalproj.orbitflow.hr.positionCategory.service;

import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.InvalidStateException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.position.repository.PositionRepository;
import com.finalproj.orbitflow.hr.positionCategory.dto.PositionCategoryOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.positionCategory.dto.PositionCategoryReqDto;
import com.finalproj.orbitflow.hr.positionCategory.dto.PositionCategoryResDto;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import com.finalproj.orbitflow.hr.positionCategory.repository.PositionCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionCategoryService
 * @since : 2025-12-22 월요일
 */

@Service
@RequiredArgsConstructor
@Transactional
public class PositionCategoryService {

    private final PositionCategoryRepository positionCategoryRepository;
    private final PositionRepository positionRepository;
    private final CompanyRepository companyRepository;

    /* ================= 생성 ================= */
    public Long create(Long companyId, PositionCategoryReqDto request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("회사를 찾을 수 없습니다."));

        if (positionCategoryRepository.existsByCompanyIdAndName(companyId, request.getName())) {
            throw new InvalidStateException("이미 존재하는 직책 카테고리명입니다.");
        }

        Integer maxOrder =
                positionCategoryRepository.findMaxActiveOrderIndexByCompanyId(companyId);

        int nextOrder = maxOrder == null ? 1 : maxOrder + 1;


        PositionCategory category =
                PositionCategory.create(company, request.getName(), nextOrder);

        return positionCategoryRepository.save(category).getId();
    }

    /* ================= 조회 ================= */
    @Transactional(readOnly = true)
    public List<PositionCategoryResDto> findAll(
            Long companyId,
            boolean includeInactive
    ) {
        List<PositionCategory> list =
                includeInactive
                        ? positionCategoryRepository.findByCompanyId(companyId)
                        : positionCategoryRepository.findByCompanyIdAndIsActiveTrue(companyId);

        return list.stream()
                .map(PositionCategoryResDto::from)
                .toList();
    }

    /* ================= 수정 ================= */
    public void update(
            Long companyId,
            Long categoryId,
            PositionCategoryReqDto request
    ) {
        PositionCategory category = getCategoryInCompany(companyId, categoryId);

        boolean wasInactive = !category.getIsActive();
        boolean willActivate = Boolean.TRUE.equals(request.getIsActive());

        if (wasInactive && willActivate) {
            // 재활성화 -> 맨 뒤로 이동
            Integer maxOrder =
                    positionCategoryRepository.findMaxActiveOrderIndexByCompanyId(companyId);
            category.updateOrderIndex(maxOrder == null ? 1 : maxOrder + 1);
        }

        // 비활성화 시 직책 사용 여부 검증
        if (Boolean.FALSE.equals(request.getIsActive())) {
            boolean used =
                    positionRepository.existsByCompanyIdAndCategoryIdAndIsActiveTrue(
                            companyId,
                            categoryId
                    );

            if (used) {
                throw new InvalidStateException(
                        "해당 카테고리를 사용하는 직책이 존재하여 비활성화할 수 없습니다."
                );
            }

            category.updateOrderIndex(null);
        }

        category.update(request.getName(), request.getIsActive());
    }

    /* ================= 정렬 ================= */
    public void updateOrder(
            Long companyId,
            List<PositionCategoryOrderUpdateReqDto.OrderItem> orders
    ) {
        if (orders == null || orders.isEmpty()) {
            throw new InvalidRequestException("순서 정보가 비어 있습니다.");
        }

        long activeCount =
                positionCategoryRepository.countByCompanyIdAndIsActiveTrue(companyId);

        if (activeCount != orders.size()) {
            throw new InvalidStateException("정렬 대상 개수가 일치하지 않습니다.");
        }

        int temp = -1;
        for (var item : orders) {
            PositionCategory category = getCategoryInCompany(companyId, item.getId());
            if (!category.getIsActive()) {
                throw new InvalidStateException("비활성 카테고리는 정렬할 수 없습니다.");
            }
            category.updateOrderIndex(temp--);
        }

        positionCategoryRepository.flush();

        int orderIndex = 1;
        for (var item : orders) {
            PositionCategory category = getCategoryInCompany(companyId, item.getId());
            category.updateOrderIndex(orderIndex++);
        }
    }




    /* ================= 내부 메서드 ================= */
    private PositionCategory getCategoryInCompany(Long companyId, Long categoryId) {
        PositionCategory category =
                positionCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new NotFoundException("직책 카테고리를 찾을 수 없습니다."));

        if (!category.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 회사의 직책 카테고리가 아닙니다.");
        }

        return category;
    }
}
