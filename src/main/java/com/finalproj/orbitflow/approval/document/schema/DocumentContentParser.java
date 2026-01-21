package com.finalproj.orbitflow.approval.document.schema;

import com.finalproj.orbitflow.approval.document.dto.CommonPayload;
import com.finalproj.orbitflow.approval.document.dto.VacationPayload;
import com.finalproj.orbitflow.approval.document.documentContent.entity.DocumentContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentParser
 * @since : 25. 12. 31. 수요일
 **/

@Component
@RequiredArgsConstructor
public class DocumentContentParser {

    private final ObjectMapper objectMapper;


    public CommonPayload extractCommon(DocumentContent content) {

        DocumentField eventField = findFirstByType(content, "event-date-range")
                .orElseThrow(() ->
                        new IllegalStateException("일정 필드를 찾을 수 없습니다.")
                );

        Map<String, Object> value = eventField.getValue();

        return new CommonPayload(
                (String) value.get("title"),
                (String) value.get("description"),
                LocalDate.parse((String) value.get("start")),
                LocalDate.parse((String) value.get("end"))
        );
    }


    public VacationPayload extractVacation(DocumentContent content) {

        DocumentField eventField = findFirstByType(content, "event-date-range")
                .orElseThrow(() ->
                        new IllegalStateException("회사 일정 필드를 찾을 수 없습니다.")
                );

        Map<String, Object> value = eventField.getValue();

        return new VacationPayload(
                value.get("vacationTypeId") == null
                        ? null
                        : Long.valueOf(value.get("vacationTypeId").toString()),
                value.get("reason").toString(),
                LocalDate.parse(value.get("start").toString()),
                LocalDate.parse(value.get("end").toString())
        );
    }


    public Optional<DocumentField> findFirstByType(
            DocumentContent content,
            String fieldType
    ) {
        try {
            Map<String, Object> root =
                    objectMapper.readValue(
                            content.getContentJson(),
                            new TypeReference<>() {
                            }
                    );

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
}
