package com.finalproj.orbitflow.hr.positionCategory.dto;

/**
 * 직책 관리 목록 + 조직 정책용 DTO
 *
 * @author : seunga03
 * @filename : PositionCategoryListDto
 * @since : 2025-12-29 월요일
 */

public record PositionCategoryListDto(
        Long id,
        String name,

        Long orgCategoryId,
        String orgCategoryName,

        Long parentPositionId,
        String parentPositionName,

        Boolean isHead,
        Boolean isActive,

        Long assignedCount,
        Integer orderIndex
) {}