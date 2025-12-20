package com.finalproj.orbitflow.hr.company.dto;

import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : BsnCheckResDto
 * @since : 2025-12-20 토요일
 */
@Getter
public class BsnCheckResDto {
    private List<Data> data;

    @Getter
    public static class Data {
        private String b_no;
        private String b_stt;    // 사업자 상태
        private String tax_type; // 과세 유형
    }
}
