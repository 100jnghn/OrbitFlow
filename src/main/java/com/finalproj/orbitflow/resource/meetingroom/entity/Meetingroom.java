package com.finalproj.orbitflow.resource.meetingroom.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.resource.status.entity.ResourceStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Meetingroom Entity
 * * @author : 종훈
 * @since : 2025-12-16
 */
@Entity
@Table(name = "meetingroom")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Meetingroom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 50)
    private String position;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_status_id")
    private ResourceStatus resourceStatus;

    public void update(String name, String position, String description, ResourceStatus resourceStatus) {
        this.name = name;
        this.position = position;
        this.description = description;
        this.resourceStatus = resourceStatus;
    }

}