package com.finalproj.orbitflow.attendance.dashboard.repository;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.dashboard.dto.DashBoardListDto;
import com.finalproj.orbitflow.attendance.dashboard.dto.SearchConditionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceDashboardRepository
 * @since : 2025. 12. 19. 금요일
 */

@Repository
public interface AttendanceDashboardRepository extends JpaRepository<Attendance,Long> {

}

