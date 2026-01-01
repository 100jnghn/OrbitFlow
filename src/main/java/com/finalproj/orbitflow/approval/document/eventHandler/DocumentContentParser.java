package com.finalproj.orbitflow.approval.document.eventHandler;

import com.finalproj.orbitflow.approval.document.dto.CommonPayload;
import com.finalproj.orbitflow.approval.document.dto.VacationPayload;
import com.finalproj.orbitflow.approval.document.schema.DocumentField;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentParser
 * @since : 25. 12. 31. 수요일
 **/

@Component

public class DocumentContentParser {

    public CommonPayload extractCommon(DocumentContent content) {

        DocumentField eventField = content.findFirstByType("event-date-range")
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

        DocumentField eventField = content.findFirstByType("event-date-range")
                .orElseThrow(() ->
                        new IllegalStateException("회사 일정 필드를 찾을 수 없습니다.")
                );

        Map<String, Object> value = eventField.getValue();

        return new VacationPayload(
                value.get("vacationTypeId") == null
                        ? null
                        : Long.valueOf(value.get("vacationTypeId").toString()),
                "휴가 사유는 공개되지 않습니다",
                LocalDate.parse(value.get("start").toString()),
                LocalDate.parse(value.get("end").toString())
        );
    }

}
