package com.finalproj.orbitflow.hr.organization.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgOrderUpdateReqDto
 * @since : 2025-12-19 금요일
 */
@Getter
public class OrgOrderUpdateReqDto {
    @NotEmpty
    private List<OrderItem> orders;

    @Getter
    public static class OrderItem {

        @NotNull
        private Long id;
    }

}
