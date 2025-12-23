package com.finalproj.orbitflow.hr.orgCategory.service;


import com.finalproj.orbitflow.global.exception.BusinessException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.InvalidStateException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryResDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryUpdateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgCategoryService
 * @since : 2025-12-17 수요일
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrgCategoryService {

    private final OrgCategoryRepository repository;
    private final OrgRepository orgRepository;

    /* ================= 목록 (검색 포함) ================= */
    @Transactional(readOnly = true)
    public List<OrgCategoryResDto> findAll(Long companyId, String keyword, boolean includeInactive) {

        List<OrgCategory> categories;

        if (includeInactive) {
            categories = repository.findByCompanyIdOrderByIsActiveDescOrderIndexAscCreatedAtDesc(companyId);
        } else {
            categories =
                    (keyword == null || keyword.isBlank())
                            ? repository.findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(companyId)
                            : repository.findByCompanyIdAndIsActiveTrueAndNameContainingIgnoreCaseOrderByOrderIndexAsc(
                            companyId, keyword.trim());
        }

        return categories.stream()
                .map(OrgCategoryResDto::from)
                .toList();
    }

    /* ================= 생성 ================= */
    public Long create(Long companyId, String rawName) {

        String name = rawName.trim();

        if (repository.existsByCompanyIdAndName(companyId, name)) {
            throw new BusinessException("이미 존재하는 조직 카테고리입니다.");
        }

        Integer max = repository.findMaxActiveOrderIndexByCompanyId(companyId);
        int nextOrder = (max == null) ? 1 : max + 1;

        return repository.save(
                OrgCategory.create(companyId, name, nextOrder)
        ).getId();
    }

    /* ================= 수정 (이름 + 활성/비활성/재활성화) ================= */
    public void update(Long companyId, Long id, OrgCategoryUpdateReqDto request) {
        OrgCategory category = get(companyId, id);

        String name = request.getName().trim();
        if (repository.existsByCompanyIdAndNameAndIdNot(companyId, name, id)) {
            throw new BusinessException("이미 존재하는 조직 카테고리입니다.");
        }

        boolean wasInactive = !category.getIsActive();
        boolean willActivate = Boolean.TRUE.equals(request.getIsActive());
        boolean willDeactivate = Boolean.FALSE.equals(request.getIsActive());

        // 재활성화
        if (wasInactive && willActivate) {
            Integer max = repository.findMaxActiveOrderIndexByCompanyId(companyId);
            category.activate(max == null ? 1 : max + 1);
        }

        // 비활성화
        if (willDeactivate) {
            if (orgRepository.existsByCompanyIdAndCategoryIdAndIsActiveTrue(companyId, id)) {
                throw new InvalidStateException(
                        "해당 카테고리를 사용하는 조직이 존재하여 비활성화할 수 없습니다."
                );
            }
            category.deactivate(); // 내부에서 orderIndex = null 처리
        }

        category.updateName(name);
    }

    /* ================= 순서 변경 ================= */
    public void updateOrder(Long companyId, List<OrgCategoryOrderUpdateReqDto.OrderItem> orders) {

        if (orders == null || orders.isEmpty()) {
            throw new InvalidRequestException("순서 정보가 비어 있습니다.");
        }

        long activeCount = repository.countByCompanyIdAndIsActiveTrue(companyId);
        if (activeCount != orders.size()) {
            throw new InvalidStateException("정렬 대상 개수가 일치하지 않습니다.");
        }

        // 1단계: 임시 order_index (충돌 방지)
        int temp = -1;
        for (var item : orders) {
            get(companyId, item.getId()).updateOrderIndex(temp--);
        }
        repository.flush();

        // 2단계: 최종 order_index
        int index = 1;
        for (var item : orders) {
            get(companyId, item.getId()).updateOrderIndex(index++);
        }
    }

    private OrgCategory get(Long companyId, Long id) {
        return repository.findByCompanyIdAndId(companyId, id)
                .orElseThrow(() -> new NotFoundException("조직 카테고리를 찾을 수 없습니다."));
    }
}
