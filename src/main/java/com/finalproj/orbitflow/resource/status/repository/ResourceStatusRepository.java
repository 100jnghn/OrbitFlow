package com.finalproj.orbitflow.resource.status.repository;

import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import com.finalproj.orbitflow.resource.status.entity.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ResourceStatusRepository
 * @since : 2025-12-16 오전 10:43 화요일
 */
public interface ResourceStatusRepository extends JpaRepository<ResourceStatus, Long> {
    ResourceStatus findByResourceStatusCode(ResourceStatusCode resourceStatusCode);
}
