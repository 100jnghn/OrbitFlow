package com.finalproj.orbitflow.hr.company.event;

import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CompanyCreatedEvent
 * @since : 2026-01-11 일요일
 */
@Getter
public class CompanyCreatedEvent {
    private final Long companyId;

    public CompanyCreatedEvent(Long companyId) {
        this.companyId = companyId;
    }
}
