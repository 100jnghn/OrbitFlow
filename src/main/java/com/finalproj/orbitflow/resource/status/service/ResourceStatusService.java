package com.finalproj.orbitflow.resource.status.service;

import com.finalproj.orbitflow.resource.status.dto.ResourceStatusResDto;
import com.finalproj.orbitflow.resource.status.repository.ResourceStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ResourceStatusService
 * @since : 2025-12-20 오후 2:52 토요일
 */
@Service
@RequiredArgsConstructor
public class ResourceStatusService {

    private final ResourceStatusRepository resourceStatusRepository;

    public List<ResourceStatusResDto> getResourceStatus() {
        return resourceStatusRepository.findAll()
                .stream()
                .map(resourceStatus -> ResourceStatusResDto.builder()
                        .id(resourceStatus.getId())
                        .resourceStatusCode(resourceStatus.getResourceStatusCode())
                        .statusName(resourceStatus.getStatusName())
                        .build()
                )
                .toList();
    }
}
