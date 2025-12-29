package com.finalproj.orbitflow.leave.leaveType.service;

import com.finalproj.orbitflow.leave.leaveType.dto.LeaveTypeResDto;
import com.finalproj.orbitflow.leave.leaveType.entity.LeaveType;
import com.finalproj.orbitflow.leave.leaveType.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveTypeService
 * @since : 2025. 12. 24. 수요일
 */

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    public List<LeaveTypeResDto> getAllSeaveTypes() {

        List<LeaveType> all = leaveTypeRepository.findAll();

        return all.stream().map(LeaveTypeResDto::from).toList();
    }
}
