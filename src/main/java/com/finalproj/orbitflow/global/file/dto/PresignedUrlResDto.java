package com.finalproj.orbitflow.global.file.dto;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PresignedUrlResDto
 * @since : 26. 1. 2. 금요일
 **/


public record PresignedUrlResDto(
        String url,
        LocalDateTime expiresAt
) {
}