package com.finalproj.orbitflow.approval.document.service.assembler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproj.orbitflow.approval.document.dto.DocumentFormFieldDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentAssembler
 * @since : 26. 1. 21. 수요일
 **/


@Component
@RequiredArgsConstructor
public class DocumentContentAssembler {

    private final ObjectMapper objectMapper;

    public DocumentContent fromSchema(
            Document document,
            FormTemplateSchema schema
    ) {
        try {
            List<DocumentFormFieldDto> fields = schema.getFields().stream()
                    .sorted(Comparator.comparing(FormFieldSchema::getOrder))
                    .map(DocumentFormFieldDto::from)
                    .toList();

            Map<String, Object> content = Map.of("fields", fields);

            String json = objectMapper.writeValueAsString(content);

            return DocumentContent.create(document, json);

        } catch (Exception e) {
            throw new IllegalStateException("문서 내용 JSON 생성 실패", e);
        }
    }
}
