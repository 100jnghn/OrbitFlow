package com.finalproj.orbitflow.attendance.leave.repository;

import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveTypeRepository
 * @since : 2025. 12. 24. 수요일
 */

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    @Query("SELECT lt FROM LeaveType lt ORDER BY lt.id")
    List<LeaveType> findAll();

    @Query("SELECT lt FROM LeaveType lt WHERE lt.isCountable = true ORDER BY lt.id")
    List<LeaveType> findByIsCountableTrueOrderByIdAsc();
}


