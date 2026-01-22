package com.finalproj.orbitflow.approval.document.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.content.dto.DocumentContentPatchReqDto;
import com.finalproj.orbitflow.approval.document.content.entity.DocumentContent;
import com.finalproj.orbitflow.approval.document.content.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 결재 문서의 작성 내용을 조회하고 수정하는 서비스.
 * <p>
 * DocumentContent에 저장된 JSON 형태의 문서 내용을
 * FormTemplateSchema로 변환하여 조회하거나,
 * 임시 저장(DRAFT) 상태의 문서에 한해 일부 필드 값을 수정한다.
 * <p>
 * 문서 내용의 구조 자체는 변경하지 않고,
 * 필드 식별자(fieldId)를 기준으로 값만 덮어쓰는 방식으로 동작한다.
 * <p>
 * 결재 진행 중인 문서는 수정이 불가능하며,
 * 수정 과정에서 문서 제목이 변경된 경우 Document 엔티티의 제목도 함께 갱신한다.
 * <p>
 * JSON 파싱 및 직렬화 오류는 시스템 오류로 간주하여 예외를 발생시킨다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentService
 * @since : 25. 12. 26. 금요일
 */


@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class DocumentContentService {
    private final DocumentContentRepository documentContentRepository;
    private final ObjectMapper objectMapper;

    public FormTemplateSchema getDocumentContentByDocumentId(Long documentId) {

        DocumentContent byDocumentId = documentContentRepository.findByDocument_Id(documentId)
                .orElseThrow(() -> new NotFoundException("Document with id: " + documentId + " not found"));


        FormTemplateSchema schema = parseSchema(byDocumentId.getContentJson());

        if (schema.getFields() == null || schema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }

        return schema;
    }


    public FormTemplateSchema parseSchema(String templateJson) {
        try {
            return objectMapper.readValue(templateJson, FormTemplateSchema.class);
        } catch (Exception e) {
            throw new IllegalStateException("template_json 파싱 실패", e);
        }
    }

    @Transactional
    public void patchContent(Long documentId, DocumentContentPatchReqDto reqDto) {
        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow(() -> new NotFoundException("문서 내용을 찾을 수 없습니다."));

        Document document = content.getDocument();

        if(document.getStatus() != DocumentStatus.DRAFT) {
            throw new ForbiddenException("결재 진행 중인 문서는 수정할 수 없습니다.");
        }

        ObjectNode root;
        try {
            root = (ObjectNode) objectMapper.readTree(content.getContentJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ArrayNode fieldsNode = (ArrayNode) root.get("fields");

        if (fieldsNode == null) {
            throw new IllegalStateException("문서 필드 정보가 없습니다.");
        }

        Map<String, JsonNode> patchMap = reqDto.getFields().stream()
                .filter(f -> f.getFieldId() != null)
                .filter(f -> f.getValue() != null)
                .collect(Collectors.toMap(
                        DocumentContentPatchReqDto.FieldValueDto::getFieldId,
                        DocumentContentPatchReqDto.FieldValueDto::getValue
                ));

        String newTitle = null;

        for (JsonNode fieldNode : fieldsNode) {
            String fieldId = fieldNode.get("fieldId").asText();

            if (!patchMap.containsKey(fieldId)) continue;

            JsonNode newValue = patchMap.get(fieldId);

            ((ObjectNode) fieldNode).set("value", newValue);

            if ("document-title".equals(fieldId)) {
                if (newValue.isTextual()) {
                    newTitle = newValue.asText().trim();
                }
            }
        }

        if (newTitle != null && !newTitle.isBlank()) {
            document.updateTitle(newTitle);
        }

        try {
            content.updateContentJson(
                    objectMapper.writeValueAsString(root)
            );
        } catch (Exception e) {
            throw new IllegalStateException("문서 JSON 직렬화 실패", e);
        }
    }
}
