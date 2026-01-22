package com.finalproj.orbitflow.approval.document.service.assembler;

import com.finalproj.orbitflow.approval.line.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.line.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.dto.PdfApprovalLineDto;
import com.finalproj.orbitflow.approval.document.dto.PdfApproverDto;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 승인 완료된 결재 문서를 PDF로 렌더링하기 위해
 * 결재선 정보를 PDF 전용 DTO 구조로 변환하는 어셈블러 클래스.
 *
 * <p>
 * 이 클래스는 특정 문서에 연결된 결재선(ApprovalLine)을
 * 순서대로 조회한 뒤, PDF 출력에 필요한 최소 정보만을 추출하여
 * {@link PdfApprovalLineDto} 및 {@link PdfApproverDto}로 변환한다.
 * </p>
 *
 * <p>
 * 결재자의 직급/직위 표시는 {@link Employee} 정보 기준으로
 * 문자열 형태로만 결정하며,
 * 서명 이미지나 파일 접근과 같은 리소스 처리는 이 단계에서 다루지 않는다.
 * 해당 처리는 PDF 렌더링 과정에서 별도의 이미지 스트림 처리 로직을 통해 수행된다.
 * </p>
 *
 * <p>
 * 즉, 이 어셈블러는
 * <ul>
 *     <li>결재선 순서 보장</li>
 *     <li>PDF 출력용 데이터 구조 변환</li>
 * </ul>
 * 에만 책임을 가지며,
 * 실제 PDF HTML 구성이나 이미지 로딩 책임과는 명확히 분리되어 있다.
 * </p>
 *
 * @author Choi MinHyeok
 * @filename PdfApprovalLineAssembler
 * @since 2026.01.05
 */


@Component
@RequiredArgsConstructor
public class PdfApprovalLineAssembler {

    private final ApprovalLineRepository approvalLineRepository;

    public PdfApprovalLineDto from(Long documentId) {

        List<ApprovalLine> approvals =
                approvalLineRepository.findByDocument_IdOrderByOrderNoAsc(documentId);

        if (approvals.isEmpty()) {
            return new PdfApprovalLineDto(List.of());
        }

        List<PdfApproverDto> approvers = approvals.stream()
                .map(this::toApproverDto)
                .toList();

        return new PdfApprovalLineDto(approvers);
    }

    private PdfApproverDto toApproverDto(ApprovalLine approvalLine) {

        Employee approver = approvalLine.getApprover();


        return new PdfApproverDto(
                approvalLine.getId(),
                approvalLine.getOrderNo(),
                approver.getName(),
                approver.getPositionCategory() != null
                        ? approver.getPositionCategory().getName()
                        : null
        );
    }
}
