package com.finalproj.orbitflow.approval.document.render.support;

import com.finalproj.orbitflow.attendance.leave.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DefaultVacationTypeNameResolver
 * @since : 26. 1. 4. 일요일
 **/


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