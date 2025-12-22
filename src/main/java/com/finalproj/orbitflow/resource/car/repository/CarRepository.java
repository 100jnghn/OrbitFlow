package com.finalproj.orbitflow.resource.car.repository;

import com.finalproj.orbitflow.resource.car.entity.Car;
import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : CarRepository
 * @since : 2025-12-16 오전 11:13 화요일
 */
public interface CarRepository extends JpaRepository<Car, Long> {

    @Query("""
                select c 
                from Car c
                where c.company.id = :companyId
                and c.resourceStatus.resourceStatusCode != 'DELETED'
            """)
    Page<Car> findAllByCompany_Id(Long companyId, Pageable pageable);

    @Query("""
                select c 
                from Car c
                where c.company.id = :companyId
                    and c.resourceStatus.resourceStatusCode = :resourceStatusCode
            """)
    List<Car> findAllByCompanyIdAndStatus(Long companyId, ResourceStatusCode resourceStatusCode);

    boolean existsByNumber(String number);
}
