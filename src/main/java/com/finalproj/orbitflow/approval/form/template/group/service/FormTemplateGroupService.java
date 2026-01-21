package com.finalproj.orbitflow.approval.form.template.group.service;

import com.finalproj.orbitflow.approval.form.template.entity.FormTemplate;
import com.finalproj.orbitflow.approval.form.template.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.form.template.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.form.template.group.dto.*;
import com.finalproj.orbitflow.approval.form.template.group.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
import com.finalproj.orbitflow.approval.form.template.group.repository.FormTemplateGroupRepository;
import com.finalproj.orbitflow.approval.form.template.category.entity.TemplateCategory;
import com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode;
import com.finalproj.orbitflow.approval.form.template.category.repository.TemplateCategoryRepository;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 결재 양식 그룹(FormTemplateGroup)에 대한
 * 생성, 조회, 수정 및 검증 로직을 담당하는 서비스 클래스이다.
 * <p>
 * 양식 그룹은 여러 개의 결재 양식을 묶는 상위 개념으로,
 * 회사 단위로 관리되며 카테고리와 기본 문서 성격(baseRole)을 기준으로
 * 양식 생성 및 AI 설계 정책의 기준이 된다.
 * <p>
 * 이 서비스에서는 다음과 같은 책임을 가진다.
 * - 회사별 양식 그룹 목록 조회 (활성 양식 기준)
 * - 양식 그룹 생성 시 카테고리와 baseRole 조합 검증
 * - 양식 그룹 상세 정보 조회
 * - 양식 그룹 활성/비활성 상태 변경
 * - 양식 그룹 비활성화 시, 연결된 활성 양식의 상태 동기화 처리
 * - 양식 그룹명 중복 여부 확인
 * <p>
 * 특히 카테고리(TemplateCategory)와 baseRole의 조합은
 * 일정/근태 도메인 정책과 직접적으로 연결되기 때문에,
 * validateCategoryAndBaseRole 메서드를 통해
 * 허용되지 않는 조합을 사전에 차단한다.
 * <p>
 * 이 클래스는 트랜잭션 경계를 기준으로
 * 조회 로직과 변경 로직을 명확히 분리하여 설계되었으며,
 * 실제 HTTP 요청/응답 처리나 권한 판단은
 * 컨트롤러 계층에서 수행한다.
 *
 * @author Choi MinHyeok
 * @filename FormTemplateGroupService
 * @since 2025. 12. 16.
 */


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormTemplateGroupService {
    private final FormTemplateGroupRepository formTemplateGroupRepository;
    private final CompanyRepository companyRepository;
    private final TemplateCategoryRepository templateCategoryRepository;
    private final FormTemplateRepository formTemplateRepository;

    public List<FormTemplateGroupListResDto> getFormTemplateGroups(
            Long companyId,
            String keyword
    ) {
        String searchKeyword = (keyword == null) ? "" : keyword;

        return formTemplateGroupRepository
                .findLatestGroupsWithActiveTemplate(companyId, searchKeyword);
    }


    @Transactional
    public FormTemplateGroupCreateResDto saveFormTemplateGroup(
            FormTemplateGroupCreateReqDto dto,
            Long companyId
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "회사를 찾을 수 없습니다. companyId = " + companyId
                ));

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "문서 양식 그룹 이름은 필수입니다."
            );
        }

        TemplateCategory category = templateCategoryRepository
                .findByCode(dto.getCategoryCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "유효하지 않은 카테고리입니다."
                ));

        validateCategoryAndBaseRole(
                category.getCode(),
                dto.getBaseRole()
        );

        if (formTemplateGroupRepository.existsByCompanyIdAndName(
                companyId, dto.getName()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 존재하는 문서 양식 그룹 이름입니다."
            );
        }

        FormTemplateGroup entity = dto.toEntity(company, category);
        formTemplateGroupRepository.save(entity);

        return new FormTemplateGroupCreateResDto(entity.getId());
    }


    public FormTemplateGroupDetailResDto getDetailTemplateGroup(Long id) {
        FormTemplateGroup target = formTemplateGroupRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "양식 그룹을 찾을 수 없습니다. formTemplateGroupId = " + id
                )
        );

        return FormTemplateGroupDetailResDto.from(target);
    }

    @Transactional
    public void updateFormTemplateGroup(FormTemplateGroupUpdateReqDto dto, Long id) {

        FormTemplateGroup group = formTemplateGroupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "양식 그룹을 찾을 수 없습니다. formTemplateGroupId = " + id
                ));

        if (dto.getDescription() != null) {
            group.changeDescription(dto.getDescription());
        }

        if (dto.getActive() != null) {

            if (!dto.getActive()) {

                group.deactivate();

                formTemplateRepository
                        .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                                group.getId(),
                                FormTemplateStatus.ACTIVE
                        )
                        .ifPresent(FormTemplate::deActive);

            }
            else {
                group.activate();
            }
        }
    }


    private void validateCategoryAndBaseRole(
            TemplateCategoryCode categoryCode,
            BaseRole baseRole
    ) {
        if (categoryCode == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "카테고리는 필수입니다."
            );
        }

        switch (categoryCode) {
            case GENERAL -> {
                if (baseRole != null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "GENERAL 카테고리는 일정 유형을 가질 수 없습니다."
                    );
                }
            }
            case SCHEDULE -> {
                if (baseRole != BaseRole.COMPANY_EVENT) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "SCHEDULE 카테고리는 회사 일정만 가능합니다."
                    );
                }
            }
            case ATTENDANCE -> {
                if (baseRole == null || baseRole == BaseRole.COMPANY_EVENT) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "ATTENDANCE 카테고리는 휴가/출장/외근 중 하나여야 합니다."
                    );
                }
            }
        }
    }

    public boolean checkFormTemplateGroupName(Long companyId, String name) {
        return formTemplateGroupRepository
                .findByCompany_IdAndName(companyId, name)
                .isEmpty();
    }
}
