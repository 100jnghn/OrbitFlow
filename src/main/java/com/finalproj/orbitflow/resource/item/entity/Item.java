package com.finalproj.orbitflow.resource.item.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.resource.itemcategory.entity.ItemCategory;
import com.finalproj.orbitflow.resource.status.entity.ResourceStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : Item
 * @since : 2025-12-16 오전 11:30 화요일
 */
@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id", nullable = false)
    private ItemCategory itemCategory;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_status_id")
    private ResourceStatus resourceStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;


    public void update(ItemCategory itemCategory, String name, String description,
                       ResourceStatus resourceStatus, File file) {
        this.itemCategory = itemCategory;
        this.name = name;
        this.description = description;
        this.resourceStatus = resourceStatus;
        this.file = file;
    }

    public void deleteFile() {
        this.file = null;
    }
}