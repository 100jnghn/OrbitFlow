package com.finalproj.orbitflow.hr.positionCategory.service;

import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.InvalidStateException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
import com.finalproj.orbitflow.hr.orgPositionUsage.repository.OrgPositionUsageRepository;
import com.finalproj.orbitflow.hr.positionCategory.dto.*;
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
    private final CompanyRepository companyRepository;
    private final OrgCategoryRepository orgCategoryRepository;
    private final OrgPositionUsageRepository orgPositionUsageRepository;

    /* ================= 생성 ================= */
    public Long create(Long companyId, PositionCategoryCreateReqDto request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("회사를 찾을 수 없습니다."));

        String name = normalizeName(request.getName());

        OrgCategory orgCategory = orgCategoryRepository.findById(request.getOrgCategoryId())
                .orElseThrow(() -> new NotFoundException("조직 카테고리를 찾을 수 없습니다."));

        if (positionCategoryRepository
                .existsByCompanyIdAndOrgCategoryIdAndName(companyId, orgCategory.getId(), name)) {
            throw new InvalidStateException("이미 존재하는 직책 카테고리입니다.");
        }

        if (!orgCategory.getCompanyId().equals(companyId)) {
            throw new ForbiddenException("해당 회사의 조직 카테고리가 아닙니다.");
        }

        // HEAD 중복 금지 로직 --> 제한 두지 않는 방향으로 결정
//        if (request.getIsHead()
//                && positionCategoryRepository.existsByCompanyIdAndOrgCategoryIdAndIsHeadTrue(
//                companyId, orgCategory.getId())) {
//            throw new InvalidStateException("해당 조직 유형에는 이미 HEAD 직책이 존재합니다.");
//        }


        PositionCategory parent = null;
        if (request.getParentPositionId() != null) {
            parent = positionCategoryRepository.findById(request.getParentPositionId())
                    .orElseThrow(() -> new NotFoundException("상위 직책을 찾을 수 없습니다."));

            // 회사 검증
            if (!parent.getCompany().getId().equals(companyId)) {
                throw new ForbiddenException("해당 회사의 직책이 아닙니다.");
            }

            // 활성 상태 검증
            if (!Boolean.TRUE.equals(parent.getIsActive())) {
                throw new InvalidStateException("비활성 직책은 상위 직책으로 선택할 수 없습니다.");
            }

            OrgCategory parentOrg = parent.getOrgCategory();

            // 회사(org root)는 항상 허용
            // 회사 조직 유형(is_root = true)은 최상위 고정 개념이므로
            // order_index 비교 대상에서 제외한다.
            if (Boolean.FALSE.equals(parentOrg.getIsRoot())) {

                Integer parentOrder = parentOrg.getOrderIndex();
                Integer currentOrder = orgCategory.getOrderIndex();

                if (parentOrder == null || currentOrder == null) {
                    throw new InvalidStateException("조직 유형 순서 정보가 올바르지 않습니다.");
                }

                if (parentOrder > currentOrder) {
                    throw new InvalidStateException(
                            "상위 직책은 선택한 조직 유형 이상에 속한 직책만 지정할 수 있습니다."
                    );
                }
            }
        }


        Integer max = positionCategoryRepository.findMaxActiveOrderIndexByCompanyId(companyId);
        int nextOrder = max == null ? 1 : max + 1;

        PositionCategory category = PositionCategory.create(
                company,
                orgCategory,
                parent,
                name,
                nextOrder,
                request.getIsHead()
        );

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
                        ? positionCategoryRepository.findByCompanyIdOrderByIsActiveDescOrderIndexAscCreatedAtDesc(companyId)
                        : positionCategoryRepository.findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(companyId);

        return list.stream()
                .map(PositionCategoryResDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PositionCategoryListDto> findAllWithAssignedCount(
            Long companyId,
            boolean includeInactive
    ) {
        return positionCategoryRepository.findAllWithAssignedCount(
                companyId,
                includeInactive
        );
    }


    /* ================= 수정 ================= */
    public void update(Long companyId, Long id, PositionCategoryUpdateReqDto request) {

        PositionCategory category = get(companyId, id);

        String name = normalizeName(request.getName());

        if (positionCategoryRepository.existsByCompanyIdAndNameAndIdNot(
                companyId, name, id)) {
            throw new InvalidStateException("이미 존재하는 직책 카테고리입니다.");
        }

        boolean wasActive = category.getIsActive();
        boolean willActivate = Boolean.TRUE.equals(request.getIsActive());
        boolean willDeactivate = Boolean.FALSE.equals(request.getIsActive());

        if (!wasActive && willActivate) {
            Integer max = positionCategoryRepository.findMaxActiveOrderIndexByCompanyId(companyId);
            category.activate(max == null ? 1 : max + 1);
        }

        if (wasActive && willDeactivate) {
            if (orgPositionUsageRepository
                    .existsByCompany_IdAndPositionCategory_Id(companyId, id)) {
                throw new InvalidStateException(
                        "해당 직책을 사용하는 조직이 존재하여 비활성화할 수 없습니다."
                );
            }
            category.deactivate();
        }

        category.updateName(name);
    }

    /* ================= 정렬 ================= */
    public void updateOrder(
            Long companyId,
            List<PositionCategoryOrderUpdateReqDto.OrderItem> orders
    ) {
        if (orders == null || orders.isEmpty()) {
            throw new InvalidRequestException("순서 정보가 비어 있습니다.");
        }

        long activeCount = positionCategoryRepository.countByCompanyIdAndIsActiveTrue(companyId);
        if (activeCount != orders.size()) {
            throw new InvalidStateException("정렬 대상 개수가 일치하지 않습니다.");
        }

        int temp = -1;
        for (var item : orders) {
            get(companyId, item.getId()).updateOrderIndex(temp--);
        }
        positionCategoryRepository.flush();

        int index = 1;
        for (var item : orders) {
            get(companyId, item.getId()).updateOrderIndex(index++);
        }
    }




    /* ================= 내부 메서드 ================= */
    private PositionCategory get(Long companyId, Long id) {
        PositionCategory pc = positionCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("직책 카테고리를 찾을 수 없습니다."));
        if (!pc.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 회사의 직책 카테고리가 아닙니다.");
        }
        return pc;
    }

    private String normalizeName(String raw) {
        if (raw == null || raw.trim().isBlank()) {
            throw new InvalidRequestException("직책명은 필수입니다.");
        }
        String name = raw.trim();
        if (name.length() > 50) {
            throw new InvalidRequestException("직책명은 50자 이하여야 합니다.");
        }
        return name;
    }

    private boolean isCycle(PositionCategory newParent, PositionCategory target) {
        PositionCategory cursor = newParent;
        while (cursor != null) {
            if (cursor.getId().equals(target.getId())) {
                return true;
            }
            cursor = cursor.getParent();
        }
        return false;
    }

}
