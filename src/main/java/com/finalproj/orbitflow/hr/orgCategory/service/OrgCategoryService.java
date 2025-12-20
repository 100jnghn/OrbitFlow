package com.finalproj.orbitflow.hr.orgCategory.service;


import com.finalproj.orbitflow.global.exception.*;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryResDto;
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

    private final OrgCategoryRepository orgCategoryRepository;
    private final OrgRepository orgRepository;

    /**
     * 조직 카테고리 생성
     */
    public Long create(Long companyId, String name) {

        if (orgCategoryRepository.existsByCompanyIdAndName(companyId, name)) {
            throw new BusinessException("이미 존재하는 조직 카테고리입니다.");
        }

        Integer maxOrder =
                orgCategoryRepository.findMaxOrderIndexByCompanyId(companyId);

        int nextOrder = (maxOrder == null) ? 1 : maxOrder + 1;

        OrgCategory category =
                OrgCategory.create(companyId, name, nextOrder);

        return orgCategoryRepository.save(category).getId();
    }


    /**
     * 조직 카테고리 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OrgCategoryResDto> findAll(Long companyId) {

        return orgCategoryRepository
                .findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(companyId)
                .stream()
                .map(OrgCategoryResDto::from)
                .toList();
    }

    /**
     * 조직 카테고리 수정
     */
    public void update(Long companyId, Long categoryId, String name) {

        OrgCategory category = orgCategoryRepository
                .findByCompanyIdAndId(companyId, categoryId)
                .orElseThrow(() -> new NotFoundException("조직 카테고리를 찾을 수 없습니다."));

        category.updateName(name);
    }

    /**
     * 조직 카테고리 비활성화
     */
    public void deactivate(Long companyId, Long categoryId) {

        OrgCategory category = orgCategoryRepository
                .findByCompanyIdAndId(companyId, categoryId)
                .orElseThrow(() -> new NotFoundException("조직 카테고리를 찾을 수 없습니다."));

        if (orgRepository.existsByCompanyIdAndCategoryIdAndIsActiveTrue(companyId, categoryId)) {
            throw new InvalidStateException(
                    "해당 카테고리를 사용하는 조직이 존재하여 비활성화할 수 없습니다."
            );
        }

        category.deactivate();
    }

    @Transactional
    public void updateOrder(Long companyId, List<OrgCategoryOrderUpdateReqDto.OrderItem> orders) {

        if (orders == null || orders.isEmpty()) {
            throw new InvalidRequestException("순서 정보가 비어 있습니다.");
        }

        long distinctCount = orders.stream()
                .map(OrgCategoryOrderUpdateReqDto.OrderItem::getId)
                .distinct()
                .count();

        if (distinctCount != orders.size()) {
            throw new InvalidRequestException("중복된 카테고리 ID가 존재합니다.");
        }

        long activeCount = orgCategoryRepository
                .countByCompanyIdAndIsActiveTrue(companyId);

        if (activeCount != orders.size()) {
            throw new InvalidStateException("정렬 대상 개수가 일치하지 않습니다.");
        }

        // 임시 order_index 할당 (충돌 방지)
        int tempIndex = -1;
        for (OrgCategoryOrderUpdateReqDto.OrderItem item : orders) {
            OrgCategory category = orgCategoryRepository
                    .findByCompanyIdAndId(companyId, item.getId())
                    .orElseThrow(() ->
                            new ForbiddenException("해당 회사에 속하지 않은 조직 카테고리입니다.")
                    );
            category.updateOrderIndex(tempIndex--);
        }

        orgCategoryRepository.flush();

        // 최종 order_index 재할당
        int orderIndex = 1;
        for (OrgCategoryOrderUpdateReqDto.OrderItem item : orders) {
            OrgCategory category = orgCategoryRepository
                    .findByCompanyIdAndId(companyId, item.getId())
                    .orElseThrow(() ->
                            new ForbiddenException("해당 회사에 속하지 않은 조직 카테고리입니다.")
                    );
            category.updateOrderIndex(orderIndex++);
        }
    }


}
