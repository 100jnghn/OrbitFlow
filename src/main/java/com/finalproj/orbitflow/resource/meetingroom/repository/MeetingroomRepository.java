package com.finalproj.orbitflow.resource.meetingroom.repository;

import com.finalproj.orbitflow.resource.enums.ResourceStatusCode;
import com.finalproj.orbitflow.resource.meetingroom.entity.Meetingroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : MeetingroomRepository
 * @since : 2025-12-16 오전 10:55 화요일
 */
public interface MeetingroomRepository extends JpaRepository<Meetingroom, Long> {

    @Query("""
                select m
                from Meetingroom m
                where m.company.id = :companyId
                and m.resourceStatus.resourceStatusCode != 'DELETED'
            """)
    List<Meetingroom> findAllByCompany_Id(@Param("companyId") Long companyId);

    @Query("""
                SELECT m
                FROM Meetingroom m
                JOIN FETCH m.resourceStatus rs
                WHERE m.company.id = :companyId
                    AND rs.resourceStatusCode = :targetStatus
            """)
    List<Meetingroom> findAllByCompanyIdAndStatus(
            @Param("companyId") Long companyId,
            @Param("targetStatus") ResourceStatusCode targetStatus
    );
}


