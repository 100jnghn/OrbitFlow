package com.finalproj.orbitflow.attendance.leave.service;

import com.finalproj.orbitflow.attendance.leave.dto.LeaveTypeResDto;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    /**
     * 모든 휴가 유형 조회
     */
    public List<LeaveTypeResDto> getAllLeaveTypes() {
        return leaveTypeRepository.findAll().stream()
                .map(LeaveTypeResDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 차감되는 휴가 유형만 조회 (연차, 반차 등)
     */
    public List<LeaveTypeResDto> getCountableLeaveTypes() {
        return leaveTypeRepository.findByIsCountableTrueOrderByIdAsc().stream()
                .map(LeaveTypeResDto::from)
                .collect(Collectors.toList());
    }

    private LeaveTypeResDto mapToDto(LeaveType leaveType) {
        return LeaveTypeResDto.builder()
                .typeId(leaveType.getId())
                .typeName(leaveType.getTypeName())
                .isCountable(leaveType.getIsCountable())
                .description(leaveType.getDescription())
                .build();
    }

    public Optional<String> findNameById(String leaveTypeId) {
        if (leaveTypeId == null) {
            return Optional.empty();
        }

        return leaveTypeRepository
                .findById(Long.valueOf(leaveTypeId))
                .map(LeaveType::getTypeName);
    }
}