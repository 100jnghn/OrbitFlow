package com.finalproj.orbitflow.attendance.leave.service;

import com.finalproj.orbitflow.attendance.leave.dto.LeaveTypeResDto;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * * @author : rlagkdus
 * @filename : LeaveTypeService
 * @since : 2025. 12. 21. 일요일
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;


    public List<LeaveTypeResDto> getAllLeaveTypes() {
        return leaveTypeRepository.findAll().stream()
                .map(LeaveTypeResDto::from)
                .collect(Collectors.toList());
    }


    public List<LeaveTypeResDto> getCountableLeaveTypes() {
        return leaveTypeRepository.findByIsCountableTrueOrderByIdAsc().stream()
                .map(LeaveTypeResDto::from)
                .collect(Collectors.toList());
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