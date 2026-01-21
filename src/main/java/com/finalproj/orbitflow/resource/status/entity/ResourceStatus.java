package com.finalproj.orbitflow.resource.status.entity;

import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import jakarta.persistence.*;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ResourceStatus
 * @since : 2025-12-16 오전 10:38 화요일
 */
@Entity
@Table(name = "resource_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ResourceStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_code", nullable = false, unique = true, length = 50)
    private ResourceStatusCode resourceStatusCode;

    @Column(name = "status_name", length = 50)
    private String statusName;
}