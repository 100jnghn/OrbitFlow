package com.finalproj.orbitflow.resource.item.repository;

import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import com.finalproj.orbitflow.resource.item.entity.Item;
import com.finalproj.orbitflow.resource.itemcategory.entity.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemRepository
 * @since : 2025-12-16 오전 11:34 화요일
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
    int countByItemCategory(ItemCategory itemCategory);

    @Query("""
                SELECT i
                FROM Item i
                JOIN i.resourceStatus rs
                WHERE i.company.id = :companyId
                  AND rs.resourceStatusCode != :deletedStatus
            """)
    Page<Item> getAllByCompanyId(
            @Param("companyId") Long companyId,
            @Param("deletedStatus") ResourceStatusCode deletedStatus,
            Pageable pageable
    );

    @Query("""
                SELECT i
                FROM Item i
                JOIN i.resourceStatus rs 
                WHERE i.company.id = :companyId
                    AND i.itemCategory.id = :categoryId
                    AND rs.resourceStatusCode != :deletedStatus
            """)
    Page<Item> getAllByCompanyIdAndItemCategoryId(Long companyId, Long categoryId, ResourceStatusCode deletedStatus, Pageable pageable);

    @Query("""
                SELECT i
                FROM Item i
                JOIN FETCH i.resourceStatus rs
                WHERE i.company.id = :companyId
                  AND rs.resourceStatusCode != :resourceStatusCode
            """)
    List<Item> getAllByCompanyIdAndStatus(
            @Param("companyId") Long companyId,
            @Param("resourceStatusCode") ResourceStatusCode resourceStatusCode
    );


    Item findItemById(Long itemId);

    @Query("""
                SELECT i
                FROM Item i
                JOIN FETCH i.resourceStatus rs 
                WHERE i.company.id = :companyId
                    AND i.itemCategory.id = :categoryId
                    AND rs.resourceStatusCode = :resourceStatusCode
            """)
    List<Item> getAllByCompanyIdAndItemCategoryIdAndStatus(Long companyId, Long categoryId, ResourceStatusCode resourceStatusCode);
}
