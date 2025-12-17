package com.finalproj.orbitflow.hr.orgCategory.service;

import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryResDto;
import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
import com.finalproj.orbitflow.hr.organization.repository.OrganizationRepository;
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
    private final OrganizationRepository organizationRepository;

    /**
     * 조직 카테고리 생성
     */
    public Long create(Long companyId, String name, Integer orderIndex) {

        if (orgCategoryRepository.existsByCompanyIdAndName(companyId, name)) {
            throw new IllegalArgumentException("이미 존재하는 조직 카테고리입니다.");
        }

        OrgCategory category =
                OrgCategory.create(companyId, name, orderIndex);

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
    public void update(
            Long companyId,
            Long categoryId,
            String name,
            Integer orderIndex
    ) {
        OrgCategory category = orgCategoryRepository
                .findByCompanyIdAndId(companyId, categoryId)
                .orElseThrow(() -> new IllegalArgumentException("조직 카테고리를 찾을 수 없습니다."));

        category.update(name, orderIndex);
    }

    /**
     * 조직 카테고리 비활성화
     */
    public void deactivate(Long companyId, Long categoryId) {

        OrgCategory category = orgCategoryRepository
                .findByCompanyIdAndId(companyId, categoryId)
                .orElseThrow(() -> new IllegalArgumentException("조직 카테고리를 찾을 수 없습니다."));

        if (organizationRepository.existsByCompanyIdAndCategoryIdAndIsActiveTrue(companyId, categoryId)) {
            throw new IllegalArgumentException(
                    "해당 카테고리를 사용하는 조직이 존재하여 비활성화할 수 없습니다."
            );
        }

        category.deactivate();
    }
}
