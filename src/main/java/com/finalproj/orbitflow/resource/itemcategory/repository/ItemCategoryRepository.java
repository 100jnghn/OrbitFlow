package com.finalproj.orbitflow.resource.itemcategory.repository;

import com.finalproj.orbitflow.resource.itemcategory.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemCategoryRepository
 * @since : 2025-12-16 오전 11:29 화요일
 */
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    Collection<ItemCategory> findAllByCompany_Id(Long companyId);
}
