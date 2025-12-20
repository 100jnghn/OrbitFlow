package com.finalproj.orbitflow.hr.company.external;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : BsnClient
 * @since : 2025-12-20 토요일
 */
@Component
@RequiredArgsConstructor
public class BsnClient {

    @Value("${external.business.api-key}")
    private String apiKey;

    @Value("${external.business.url}")
    private String url;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getBusinessStatus(String businessNumber) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "b_no", List.of(businessNumber)
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        url + "?serviceKey=" + apiKey,
                        request,
                        Map.class
                );

        List<Map<String, Object>> data =
                (List<Map<String, Object>>) response.getBody().get("data");

        return (String) data.get(0).get("b_stt"); // 계속사업자 / 휴업자 / 폐업자
    }
}
