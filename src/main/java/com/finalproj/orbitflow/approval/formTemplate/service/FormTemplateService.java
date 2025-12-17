package com.finalproj.orbitflow.approval.formTemplate.service;

import com.finalproj.orbitflow.approval.formTemplate.dto.FormTemplateActiveListResDto;
import com.finalproj.orbitflow.approval.formTemplate.dto.FormTemplateUpdateReqDto;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateListView;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.formTemplateGroup.repository.FormTemplateGroupRepository;
import com.finalproj.orbitflow.approval.templateCategory.entity.TemplateCategory;
import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
import com.finalproj.orbitflow.approval.templateCategory.repository.TemplateCategoryRepository;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 결재 양식(FormTemplate)의 생명주기 전반을 관리하는 서비스 클래스.
 * -
 * 주요 책임:
 * - 결재 양식 초안 생성 (버전 관리 포함)
 * - 결재 양식 구조(JSON), 카테고리, 영향 태그 수정
 * - 결재선 규칙 저장
 * - 결재 양식 최종 활성화(publish) 및 기존 ACTIVE 양식 비활성화
 * -
 * 설계 원칙:
 * - DRAFT 상태에서만 수정 가능
 * - publish는 단순 상태 변경이 아닌 비즈니스 행위(Command)
 * - ACTIVE 양식은 템플릿 그룹당 하나만 유지
 * - 모든 수정/활성화 시 회사 소속 검증 수행
 * -
 * 이 클래스는 CRUD 중심이 아닌
 * "결재 양식 도메인의 유스케이스 단위 서비스"를 지향한다.
 * *
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateService
 * @since : 25. 12. 17. 수요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormTemplateService {

    private final FormTemplateRepository formTemplateRepository;
    private final FormTemplateGroupRepository formTemplateGroupRepository;
    private final TemplateCategoryRepository templateCategoryRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public Long saveFormTemplate(
            Long templateGroupId,
            Long companyId,
            TemplateCategoryCode categoryCode
    ) {
        FormTemplateGroup group = findGroupAndCheckCompany(templateGroupId, companyId);

        TemplateCategory category = templateCategoryRepository.findByCode(categoryCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "양식 카테고리를 찾을 수 없습니다."
                ));

        int nextVersion = calculateNextVersion(templateGroupId);

        return createDraftTemplate(
                group,
                nextVersion,
                category,
                "{}",
                null,
                null
        );
    }


    @Transactional
    public Long reviseFormTemplateByTemplateGroup(Long templateGroupId, Long companyId) {

        FormTemplateGroup group = findGroupAndCheckCompany(templateGroupId, companyId);

        FormTemplate active = formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        templateGroupId,
                        FormTemplateStatus.ACTIVE
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "ACTIVE 상태의 양식을 찾을 수 없습니다."
                ));

        int nextVersion = calculateNextVersion(templateGroupId);

        return createDraftTemplate(
                group,
                nextVersion,
                active.getTemplateCategory(),
                active.getTemplateJson(),
                active.getApprovalRuleJson(),
                active.getAffectTags()
        );
    }

    private FormTemplateGroup findGroupAndCheckCompany(Long templateGroupId, Long companyId) {
        FormTemplateGroup group = formTemplateGroupRepository.findById(templateGroupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "양식 그룹을 찾을 수 없습니다."
                ));

        if (!group.getCompany().getId().equals(companyId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "해당 회사의 양식 그룹이 아닙니다."
            );
        }
        return group;
    }

    private int calculateNextVersion(Long templateGroupId) {
        return formTemplateRepository
                .findMaxVersionByTemplateGroup_Id(templateGroupId)
                .orElse(0) + 1;
    }


    private Long createDraftTemplate(
            FormTemplateGroup group,
            int version,
            TemplateCategory category,
            String templateJson,
            String approvalRuleJson,
            String affectTags
    ) {
        FormTemplate template = FormTemplate.builder()
                .company(group.getCompany())
                .templateGroup(group)
                .version(version)
                .templateCategory(category)
                .status(FormTemplateStatus.DRAFT)
                .templateJson(templateJson)
                .approvalRuleJson(approvalRuleJson)
                .affectTags(affectTags)
                .build();

        formTemplateRepository.save(template);
        return template.getId();
    }


    @Transactional
    public void updateStructure(Long formTemplateId, Long companyId, FormTemplateUpdateReqDto reqDto) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);

        checkCompany(companyId, formTemplate);

        if (reqDto.getCategoryCode() != null) {
            TemplateCategory category = templateCategoryRepository
                    .findByCode(reqDto.getCategoryCode())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "양식 카테고리를 찾을 수 없습니다."
                    ));

            formTemplate.changeCategory(category);
        }

        if (reqDto.getTemplateJson() != null) {
            formTemplate.updateTemplateJson(reqDto.getTemplateJson());
        }

        if (reqDto.getAffectTags() != null) {
            formTemplate.updateAffectTags(reqDto.getAffectTags());
        }
    }

    public void updateApprovalRule(Long formTemplateId, Long companyId, FormTemplateUpdateReqDto reqDto) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);

        checkCompany(companyId, formTemplate);

        if (reqDto.getApprovalRuleJson() != null) {
            formTemplate.updateApprovalRuleJson(reqDto.getApprovalRuleJson());
        }
    }

    @Transactional
    public void publishFormTemplate(Long formTemplateId, Long companyId) {

        FormTemplate newTemplate = findFormTemplate(formTemplateId);

        checkCompany(companyId, newTemplate);

        if (newTemplate.getStatus() != FormTemplateStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "DRAFT 상태의 결재 양식만 활성화할 수 있습니다."
            );
        }

        if (newTemplate.getTemplateJson() == null || newTemplate.getTemplateJson().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "결재 양식 구조가 입력되지 않았습니다."
            );
        }

        if (newTemplate.getApprovalRuleJson() == null || newTemplate.getApprovalRuleJson().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "결재선 규칙이 설정되지 않았습니다."
            );
        }

        formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        newTemplate.getTemplateGroup().getId(),
                        FormTemplateStatus.ACTIVE
                )
                .ifPresent(activeTemplate ->
                        activeTemplate.updateStatus(FormTemplateStatus.INACTIVE)
                );

        newTemplate.updateStatus(FormTemplateStatus.ACTIVE);
    }




    private FormTemplate findFormTemplate(Long formTemplateId) {
        return formTemplateRepository.findById(formTemplateId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "결재 양식을 찾을 수 없습니다."
                ));
    }

    private static void checkCompany(Long companyId, FormTemplate formTemplate) {
        if (!formTemplate.getCompany().getId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "사용자의 소속(회사) 와 문서 양식의 주인(회사)가 일치하지 않습니다.");
        }
    }

    public List<FormTemplateActiveListResDto> getActiveFormTemplates(Long companyId, String keyword) {
        String searchKeyword = (keyword == null) ? "" : keyword;


        List<FormTemplateListView> views = formTemplateRepository.findWithActiveTemplateAndCompanyAndKeyword(companyId, searchKeyword);
        return views.stream()
                .map(v -> new FormTemplateActiveListResDto(v.getId(), v.getName()))
                .toList();
    }
}
