package com.finalproj.orbitflow.approval.document.repository;

import com.finalproj.orbitflow.approval.document.dto.DocumentListReqDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentListResDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentMyApprovalListResDto;
import com.finalproj.orbitflow.approval.document.dto.ReferenceSearchResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentRepositoryCustom
 * @since : 25. 12. 22. 월요일
 **/


public interface DocumentRepositoryCustom {
    Page<DocumentListResDto> findMyWrittenDocuments(
            Long companyId,
            Long employeeId,
            DocumentListReqDto reqDto,
            Pageable pageable
    );

    Page<DocumentMyApprovalListResDto> findMyApprovalDocuments(Long companyId, Long employeeId, DocumentListReqDto reqDto, Pageable pageable);

    List<ReferenceSearchResDto> searchReference(
            Long employeeId,
            Long companyId,
            String keyword,
            int limit
    );
}
