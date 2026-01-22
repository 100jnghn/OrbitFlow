package com.finalproj.orbitflow.approval.form.template.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.finalproj.orbitflow.approval.form.template.dto.*;
import com.finalproj.orbitflow.approval.form.template.entity.FormTemplate;
import com.finalproj.orbitflow.approval.form.template.enums.AffectTag;
import com.finalproj.orbitflow.approval.form.template.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.form.template.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import com.finalproj.orbitflow.approval.form.template.group.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.form.template.group.repository.FormTemplateGroupRepository;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Optional;

/**
 * 결재 양식(FormTemplate)의 생성부터 수정, 활성화, 조회까지
 * 양식의 전체 생명주기를 관리하는 도메인 서비스 클래스이다.
 * <p>
 * 이 서비스는 단순한 CRUD 처리가 아니라,
 * 결재 양식 도메인에서 정의한 비즈니스 규칙과 흐름을
 * 유스케이스 단위로 구현하는 것을 목표로 한다.
 * <p>
 * 주요 책임은 다음과 같다.
 * - 결재 양식 초안(DRAFT) 생성 및 버전 관리
 * - 기존 양식을 기준으로 한 개정(복제) 흐름 제공
 * - 양식 구조(templateJson) 및 결재선 규칙(approvalRuleJson) 수정
 * - 결재 양식 활성화(publish) 처리 및 기존 ACTIVE 양식 비활성화
 * - 회사 단위 권한 검증 및 상태(DRAFT/ACTIVE/INACTIVE) 제약 관리
 * <p>
 * 설계 상 다음 원칙을 따른다.
 * - 결재 양식은 DRAFT 상태에서만 수정 가능하다.
 * - publish는 단순한 상태 변경이 아닌 명확한 비즈니스 행위(Command)이다.
 * - 하나의 양식 그룹(FormTemplateGroup)에는 항상 하나의 ACTIVE 양식만 존재한다.
 * - 모든 변경 작업은 회사 소속 검증을 선행한다.
 * <p>
 * 이 서비스는 컨트롤러나 저장소 중심의 구조가 아니라,
 * 결재 양식 도메인의 규칙과 흐름을 중심으로 한
 * 핵심 비즈니스 서비스 계층 역할을 수행한다.
 *
 * @author Choi MinHyeok
 * @filename FormTemplateService
 * @since 2025. 12. 17.
 */


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FormTemplateService {

    private final FormTemplateRepository formTemplateRepository;
    private final FormTemplateGroupRepository formTemplateGroupRepository;

    private final SampleDataGenerator sampleDataGenerator;
    private final ObjectMapper objectMapper;


    @Transactional
    public Long saveFormTemplate(
            Long templateGroupId,
            Long companyId
    ) {
        FormTemplateGroup group = findGroupAndCheckCompany(templateGroupId, companyId);

        if (!group.getActive()) {
            throw new InvalidRequestException(
                    "비활성화된 양식 그룹에서는 결재 양식을 생성할 수 없습니다."
            );
        }

        Optional<FormTemplate> existingDraft =
                formTemplateRepository
                        .findTopByTemplateGroup_IdAndStatusOrderByUpdatedAtDesc(
                                templateGroupId,
                                FormTemplateStatus.DRAFT
                        );

        if (existingDraft.isPresent()) {
            return existingDraft.get().getId();
        }

        int baseVersion = formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        templateGroupId,
                        FormTemplateStatus.ACTIVE
                )
                .map(FormTemplate::getVersion)
                .orElse(1);

        List<AffectTag> affectTags = buildAffectTags(group);

        return createDraftTemplate(
                group,
                baseVersion,
                buildInitialTemplateJson(),
                buildInitialApprovalRuleJson(),
                affectTags
        );
    }


    @Transactional
    public Long reviseFormTemplateByTemplateGroup(Long groupId, Long companyId) {
        FormTemplateGroup group = findGroupAndCheckCompany(groupId, companyId);

        if (!group.getActive()) {
            throw new InvalidRequestException(
                    "비활성화된 양식 그룹에서는 결재 양식을 수정할 수 없습니다."
            );
        }

        Optional<FormTemplate> draft =
                formTemplateRepository
                        .findTopByTemplateGroup_IdAndStatusOrderByUpdatedAtDesc(
                                groupId, FormTemplateStatus.DRAFT
                        );

        if (draft.isPresent()) {
            return draft.get().getId();
        }

        Optional<FormTemplate> active =
                formTemplateRepository
                        .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                                groupId, FormTemplateStatus.ACTIVE
                        );

        FormTemplate base;

        base = active.orElseGet(() -> formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        groupId, FormTemplateStatus.INACTIVE
                )
                .orElseThrow(() ->
                        new InvalidRequestException(
                                "복제할 기준 양식이 없습니다."
                        )
                ));

        return createDraftTemplate(
                group,
                base.getVersion(),
                base.getTemplateJson(),
                base.getApprovalRuleJson(),
                base.getAffectTags()
        );
    }


    @Transactional
    public void updateStructure(
            Long formTemplateId,
            Long companyId,
            FormTemplateUpdateReqDto reqDto
    ) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);
        checkCompany(companyId, formTemplate);
        checkStatusIsDraft(formTemplate);


        try {
            if (reqDto.getTemplateJson() != null) {
                formTemplate.updateTemplateJson(
                        objectMapper.writeValueAsString(reqDto.getTemplateJson())
                );
            }
        } catch (Exception e) {
            throw new InvalidRequestException("templateJson 형식이 올바르지 않습니다.");
        }
    }

    @Transactional
    public void updateApprovalRule(
            Long formTemplateId,
            Long companyId,
            FormTemplateUpdateReqDto reqDto
    ) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);
        checkCompany(companyId, formTemplate);
        checkStatusIsDraft(formTemplate);


        if (reqDto.getApprovalRuleJson() != null) {
            try {
                formTemplate.updateApprovalRuleJson(
                        objectMapper.writeValueAsString(reqDto.getApprovalRuleJson())
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Transactional
    public void publishFormTemplate(Long formTemplateId, Long companyId) {
        FormTemplate draft = findFormTemplate(formTemplateId);
        checkCompany(companyId, draft);

        checkStatusIsDraft(draft);

        int nextVersion =
                calculateNextActiveVersion(draft.getTemplateGroup().getId());

        log.info("nextVersion = {}", nextVersion);

        formTemplateRepository
                .findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(
                        draft.getTemplateGroup().getId(),
                        FormTemplateStatus.ACTIVE
                )
                .ifPresent(active -> {
                    active.updateStatus(FormTemplateStatus.INACTIVE);
                    formTemplateRepository.flush();
                });

        draft.updateVersion(nextVersion);
        draft.updateStatus(FormTemplateStatus.ACTIVE);
    }

    private static void checkStatusIsDraft(FormTemplate draft) {
        if (draft.getStatus() != FormTemplateStatus.DRAFT) {
            throw new InvalidRequestException(
                    "DRAFT 상태의 결재 양식만 수정 또는 활성화할 수 있습니다."
            );
        }

        if (!draft.getTemplateGroup().getActive()) {
            throw new InvalidRequestException(
                    "활성 상태의 양식 그룹만 수정 또는 활성화할 수 있습니다."
            );
        }
    }


    public List<FormTemplateActiveListResDto> getActiveFormTemplates(
            Long companyId,
            String keyword
    ) {
        String searchKeyword = keyword == null ? "" : keyword;

        return formTemplateRepository
                .findWithActiveTemplateAndCompanyAndKeyword(companyId, searchKeyword)
                .stream()
                .map(v ->
                        new FormTemplateActiveListResDto(
                                v.getId(),
                                v.getVersion(),
                                v.getGroupId(),
                                v.getName()
                        )
                )
                .toList();
    }

    public FormTemplateDetailResDto getDetailFormTemplate(
            Long formTemplateId,
            Long companyId
    ) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);
        checkCompany(companyId, formTemplate);

        return FormTemplateDetailResDto.from(formTemplate, objectMapper);
    }

    public Page<FormTemplateAllListResDto> allFormTemplate(
            Long companyId,
            int size,
            int offset,
            FormTemplateAllListReqDto reqDto
    ) {
        Pageable pageable =
                PageRequest.of(offset, size, Sort.by(Sort.Direction.DESC, "updatedAt"));


        if (reqDto.getKeyword() == null) {
            reqDto.setKeyword("");
        }

        return formTemplateRepository
                .findAllWithDocumentCount(companyId, reqDto.getKeyword(), reqDto.getStatus(), reqDto.getTemplateCategoryCode(), pageable)
                .map(FormTemplateAllListResDto::from);
    }


    public FormTemplatePreviewResDto getPreviewFormTemplate(
            Long companyId,
            Long formTemplateId
    ) {
        FormTemplate formTemplate = findFormTemplate(formTemplateId);
        checkCompany(companyId, formTemplate);

        FormTemplateSchema contentSchema =
                parseSchema(formTemplate.getTemplateJson());

        sampleDataGenerator.fillValues(contentSchema);

        FormTemplateMetaDto meta =
                FormTemplateMetaDto.from(formTemplate);

        return FormTemplatePreviewResDto.from(meta, contentSchema);
    }

    @Transactional
    public void deleteDraftFormTemplate(Long formTemplateId) {

        FormTemplate template = findFormTemplate(formTemplateId);

        if (template.getStatus() != FormTemplateStatus.DRAFT) {
            throw new InvalidRequestException("임시 상태의 양식만 삭제할 수 있습니다.");
        }

        FormTemplateGroup group = template.getTemplateGroup();

        long templateCount =
                formTemplateRepository.countByTemplateGroup_Id(group.getId());

        if (templateCount <= 1) {
            throw new InvalidRequestException(
                    "양식 그룹에는 최소 1개의 양식이 필요합니다. 삭제할 수 없습니다."
            );
        }

        formTemplateRepository.delete(template);
    }




    private List<AffectTag> buildAffectTags(FormTemplateGroup group) {
        return switch (group.getTemplateCategory().getCode()) {
            case GENERAL -> List.of();
            case SCHEDULE -> List.of(AffectTag.SCHEDULE);
            case ATTENDANCE -> List.of(AffectTag.ATTENDANCE);
        };
    }


    private FormTemplate findFormTemplate(Long formTemplateId) {
        return formTemplateRepository.findById(formTemplateId)
                .orElseThrow(() ->
                        new NotFoundException("결재 양식을 찾을 수 없습니다.")
                );
    }

    private FormTemplateGroup findGroupAndCheckCompany(
            Long templateGroupId,
            Long companyId
    ) {
        FormTemplateGroup group = formTemplateGroupRepository.findById(templateGroupId)
                .orElseThrow(() ->
                        new NotFoundException("양식 그룹을 찾을 수 없습니다.")
                );

        if (!group.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("해당 회사의 양식 그룹이 아닙니다.");
        }
        return group;
    }

    private static void checkCompany(Long companyId, FormTemplate formTemplate) {
        if (!formTemplate.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException(
                    "사용자의 소속(회사)와 문서 양식의 회사가 일치하지 않습니다."
            );
        }
    }

    private int calculateNextActiveVersion(Long templateGroupId) {
        return formTemplateRepository
                .findMaxVersionByTemplateGroupId(templateGroupId)
                .orElse(0) + 1;
    }

    private Long createDraftTemplate(
            FormTemplateGroup group,
            int version,
            String templateJson,
            String approvalRuleJson,
            List<AffectTag> affectTags
    ) {
        FormTemplate template = FormTemplate.builder()
                .company(group.getCompany())
                .templateGroup(group)
                .version(version)
                .status(FormTemplateStatus.DRAFT)
                .templateJson(templateJson)
                .approvalRuleJson(approvalRuleJson)
                .affectTags(affectTags == null ? List.of() : affectTags)
                .build();

        formTemplateRepository.save(template);
        return template.getId();
    }

    private String buildInitialTemplateJson() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode fields = root.putArray("fields");

        ObjectNode titleField = objectMapper.createObjectNode();
        titleField.put("fieldId", "document-title");
        titleField.put("fieldType", "document-title");
        titleField.put("label", "문서 제목");
        titleField.put("required", true);
        titleField.put("order", 1);

        ObjectNode meta = titleField.putObject("meta");
        meta.put("placeholder", "문서 제목을 입력하세요.");
        meta.put("maxLength", 100);

        fields.add(titleField);
        return root.toString();
    }

    private String buildInitialApprovalRuleJson() {
        ArrayNode rootArray = objectMapper.createArrayNode();

        ObjectNode firstStep = objectMapper.createObjectNode();
        firstStep.put("step", 1);
        firstStep.putNull("organizationId");
        firstStep.putNull("positionCategoryId");

        rootArray.add(firstStep);
        return rootArray.toString();
    }

    private FormTemplateSchema parseSchema(String templateJson) {
        try {
            return objectMapper.readValue(templateJson, FormTemplateSchema.class);
        } catch (Exception e) {
            throw new IllegalStateException("template_json 파싱 실패", e);
        }
    }
}
