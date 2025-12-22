package com.finalproj.orbitflow.hr.company.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CompanySignupRequest
 * @since : 2025-12-16 화요일
 */
@Getter
@NoArgsConstructor
public class CompanySignupReqDto {
    private String companyName;
    private String address;
    private String representativeName;
    private String representativeContact;
    private String adminEmail;
    private String adminPassword;
    private String businessNumber;
}