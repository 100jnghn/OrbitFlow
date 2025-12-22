package com.finalproj.orbitflow.resource.status.dto;

import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ResourceStatusResDto
 * @since : 2025-12-20 오후 2:54 토요일
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceStatusResDto {

    private Long id;
    private ResourceStatusCode resourceStatusCode;
    private String statusName;
}
