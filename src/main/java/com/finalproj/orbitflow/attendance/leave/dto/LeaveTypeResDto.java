package com.finalproj.orbitflow.attendance.leave.dto;

import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeResDto {
    private Long typeId;
    private String typeName;
    private Boolean isCountable;
    private String description;

    public static LeaveTypeResDto from(LeaveType leaveType) {
        return LeaveTypeResDto.builder()
                .typeId(leaveType.getId())
                .typeName(leaveType.getTypeName())
                .isCountable(leaveType.getIsCountable())
                .description(leaveType.getDescription())
                .build();
    }
}

