package com.finalproj.orbitflow.resource.meetingroom.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.resource.status.entity.ResourceStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : Meetingroom
 * @since : 2025-12-16 오전 10:48 화요일
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

    public void delete(ResourceStatus deletedStatus) {
        this.resourceStatus = deletedStatus;
    }
}
