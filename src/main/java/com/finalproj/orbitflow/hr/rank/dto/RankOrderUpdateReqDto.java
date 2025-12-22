package com.finalproj.orbitflow.hr.rank.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankOrderUpdateReqDto
 * @since : 2025-12-20 토요일
 */
@Getter
public class RankOrderUpdateReqDto {
    @NotEmpty
    private List<OrderItem> orders;

    @Getter
    public static class OrderItem {
        @NotNull
        private Long id;
    }
}
