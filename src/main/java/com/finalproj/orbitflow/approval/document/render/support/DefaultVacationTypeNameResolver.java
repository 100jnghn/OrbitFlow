package com.finalproj.orbitflow.approval.document.render.support;

import com.finalproj.orbitflow.attendance.leave.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 휴가 타입 ID를 표시용 휴가 이름으로 변환하는 기본 구현체.
 * <p>
 * PDF 렌더링이나 문서 출력 과정에서 사용되는 휴가 타입 값은
 * 내부적으로는 ID 형태로 관리되기 때문에,
 * 실제 사용자에게 보여줄 이름으로 변환하는 책임을 분리하기 위해
 * VacationTypeNameResolver 인터페이스를 구현한다.
 * <p>
 * 이 구현체는 LeaveTypeService를 통해 휴가 타입 ID를 조회하며,
 * 조회되지 않는 경우에는 기본값("-")을 반환한다.
 * <p>
 * 렌더링 계층은 휴가 타입의 저장 방식이나 조회 로직을 알 필요 없이
 * Resolver를 통해 일관된 방식으로 표시 이름만을 얻도록 설계되었다.
 *
 * @author : Choi MinHyeok
 * @filename : DefaultVacationTypeNameResolver
 * @since : 26. 1. 4. 일요일
 */


@Component
@RequiredArgsConstructor
public class DefaultVacationTypeNameResolver
        implements VacationTypeNameResolver {

    private final LeaveTypeService leaveTypeService;

    @Override
    public String resolve(String vacationTypeId) {
        return leaveTypeService
                .findNameById(vacationTypeId)
                .orElse("-");
    }
}