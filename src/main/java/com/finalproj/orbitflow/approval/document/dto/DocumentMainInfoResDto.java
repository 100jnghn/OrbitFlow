package com.finalproj.orbitflow.approval.document.dto;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentMainInfoResDto
 * @since : 26. 1. 15. 목요일
 **/


public record DocumentMainInfoResDto(
        int myTurnWaitingCount,        // 🔴 지금 내가 결재해야 하는 문서
        int waitingBeforeMyTurnCount,  // ⏳ 앞 결재자 처리 중인 내 결재 문서
        int progressCount,             // 내가 상신한 진행 중 문서
        int rejectCount,               // 반려
        int monthApprovedCount,         // 이번 달 승인 완료
        int beforeMonthApprovedCount    // 저번 달 승인 완료
) {
}
