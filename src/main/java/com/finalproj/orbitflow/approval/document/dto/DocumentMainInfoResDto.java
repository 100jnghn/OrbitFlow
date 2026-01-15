package com.finalproj.orbitflow.approval.document.dto;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentMainInfoResDto
 * @since : 26. 1. 15. 목요일
 **/


public record DocumentMainInfoResDto(
        int waitingCount,          // 내 결재 대기
        int progressCount,         // 내가 작성한 진행 중
        int rejectCount,           // 반려
        int monthApprovedCount,     // 이번 달 승인 완료
        int beforeMonthApprovedCount     // 저번 달 승인 완료
) {
}
