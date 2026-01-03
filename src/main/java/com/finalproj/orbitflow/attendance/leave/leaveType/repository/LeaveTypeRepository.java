package com.finalproj.orbitflow.attendance.leave.leaveType.repository;

import com.finalproj.orbitflow.attendance.leave.leaveType.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    
    /**
     * 모든 휴가 유형 조회
     */
    @Query("SELECT lt FROM LeaveType lt ORDER BY lt.id")
    List<LeaveType> findAll();
    
    /**
     * is_countable = 1 (true)인 휴가 유형만 조회
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.isCountable = true ORDER BY lt.id")
    List<LeaveType> findByIsCountableTrueOrderByIdAsc();
}


