package com.finalproj.orbitflow.global.time.calendar.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproj.orbitflow.global.time.calendar.service.HolidayProvider;
import com.finalproj.orbitflow.global.time.calendar.service.impl.props.HolidayApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : KoreaPublicHolidayApiProvider
 * @since : 26. 1. 6. 화요일
 **/


@Component
@RequiredArgsConstructor
public class KoreaPublicHolidayApiProvider implements HolidayProvider {

    private final RestTemplate restTemplate;
    private final HolidayApiProperties properties;

    @Qualifier("holidayObjectMapper")
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    public Map<LocalDate, String> getHolidays(int year) {

        Map<LocalDate, String> result = new HashMap<>();

        for (int month = 1; month <= 12; month++) {

            String url =
                    properties.getUrl()
                            + "?serviceKey=" + properties.getApiKey()
                            + "&solYear=" + year
                            + "&solMonth=" + String.format("%02d", month)
                            + "&_type=json"
                            + "&numOfRows=50";

            try {
                String json =
                        restTemplate.getForObject(url, String.class);

                if (json == null || json.isBlank()) continue;

                JsonNode root = objectMapper.readTree(json);

                JsonNode items =
                        root.path("response")
                                .path("body")
                                .path("items");

                // items: "" or 없음
                if (items.isMissingNode() || items.isTextual()) {
                    continue;
                }

                JsonNode itemNode = items.path("item");
                if (itemNode.isMissingNode()) continue;

                if (itemNode.isArray()) {
                    for (JsonNode node : itemNode) {
                        extractHoliday(node, result);
                    }
                } else if (itemNode.isObject()) {
                    extractHoliday(itemNode, result);
                }

            } catch (Exception e) {
                throw new IllegalStateException(
                        "공휴일 API 파싱 실패: year=" + year + ", month=" + month,
                        e
                );
            }
        }

        return result;
    }

    private void extractHoliday(JsonNode node, Map<LocalDate, String> result) {

        if (!"Y".equals(node.path("isHoliday").asText())) {
            return;
        }

        LocalDate date = LocalDate.parse(
                node.path("locdate").asText(),
                FORMATTER
        );

        result.put(date, node.path("dateName").asText());
    }
}
