package com.finalproj.orbitflow.approval.document.repository;

import com.finalproj.orbitflow.approval.document.dto.DocumentListReqDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentListResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentRepositoryCustom
 * @since : 25. 12. 22. 월요일
 **/


public interface DocumentRepositoryCustom {
    Page<DocumentListResDto> findMyDocuments(
            Long companyId,
            Long employeeId,
            DocumentListReqDto reqDto,
            Pageable pageable
    );
}
