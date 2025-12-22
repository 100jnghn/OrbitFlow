package com.finalproj.orbitflow.resource.car.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.resource.meetingroom.entity.Meetingroom;
import com.finalproj.orbitflow.resource.status.entity.ResourceStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : Car
 * @since : 2025-12-16 오전 11:04 화요일
 */
@Entity
@Table(name = "car")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Car extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 15)
    private String number;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "driver_age", nullable = false)
    private Integer driverAge;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_status_id")
    private ResourceStatus resourceStatus;

    // 1 : 1 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    public void update(String number, String name, Integer driverAge, String description,
                       ResourceStatus resourceStatus, File file) {
        this.number = number;
        this.name = name;
        this.driverAge = driverAge;
        this.description = description;
        this.resourceStatus = resourceStatus;
        this.file = file;
    }

    public void delete(ResourceStatus deletedStatus) {
        this.resourceStatus = deletedStatus;
    }
}