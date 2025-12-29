package com.finalproj.orbitflow.leave.leaveType.repository;

import com.finalproj.orbitflow.leave.leaveType.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    List<LeaveType> findAll();
}

