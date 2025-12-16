package com.finalproj.orbitflow.resource.meetingroom.repository;

import com.finalproj.orbitflow.resource.meetingroom.entity.Meetingroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
                and m.resourceStatus != "DELETED"
            """)
    List<Meetingroom> findAllByCompany_Id(Long companyId);
}
