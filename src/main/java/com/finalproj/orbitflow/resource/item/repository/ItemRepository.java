package com.finalproj.orbitflow.resource.item.repository;

import com.finalproj.orbitflow.resource.item.entity.Item;
import com.finalproj.orbitflow.resource.itemcategory.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemRepository
 * @since : 2025-12-16 오전 11:34 화요일
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
    int countByItemCategory(ItemCategory itemCategory);
}
