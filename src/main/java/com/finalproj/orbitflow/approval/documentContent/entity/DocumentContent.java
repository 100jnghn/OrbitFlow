package com.finalproj.orbitflow.approval.documentContent.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : DocumentContent
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.document.dto.DocumentFormFieldDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.schema.DocumentField;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Entity
@Table(name = "document_content")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentContent extends BaseEntity {

    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(name = "content_json", nullable = false, columnDefinition = "json")
    private String contentJson;

    public void updateContentJson(String updateJson) {
        this.contentJson = updateJson;
    }

    public static DocumentContent fromSchema(
            Document document,
            FormTemplateSchema schema,
            ObjectMapper objectMapper
    ) {
        List<DocumentFormFieldDto> fields = schema.getFields().stream()
                .sorted(Comparator.comparing(FormFieldSchema::getOrder))
                .map(DocumentFormFieldDto::from)
                .toList();

        Map<String, Object> content = Map.of("fields", fields);

        try {
            return DocumentContent.builder()
                    .document(document)
                    .contentJson(objectMapper.writeValueAsString(content))
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("문서 내용 JSON 생성 실패", e);
        }
    }

    public Optional<DocumentField> findFirstByType(String fieldType) {
        try {
            Map<String, Object> root =
                    objectMapper.readValue(contentJson, new TypeReference<>() {
                    });

            List<Map<String, Object>> fields =
                    (List<Map<String, Object>>) root.get("fields");

            if (fields == null) {
                return Optional.empty();
            }

            return fields.stream()
                    .filter(f -> fieldType.equals(f.get("fieldType")))
                    .findFirst()
                    .map(f -> new DocumentField(
                            (String) f.get("fieldId"),
                            (String) f.get("fieldType"),
                            (String) f.get("label"),
                            (Map<String, Object>) f.get("value")
                    ));
        } catch (Exception e) {
            throw new IllegalStateException("문서 내용 파싱 실패", e);
        }
    }


    public static DocumentContent revise(Document rejected, DocumentContent before) {
        return DocumentContent.builder()
                .document(rejected)
                .contentJson(before.contentJson)
                .build();
    }
}
