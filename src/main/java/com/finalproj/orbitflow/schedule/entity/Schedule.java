package com.finalproj.orbitflow.schedule.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.schedule.dto.ScheduleReqDto;
import com.finalproj.orbitflow.schedule.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : Schedule
 * @since : 2025-12-16 오후 1:12 화요일
 */
@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "is_company", nullable = false)
    private boolean isCompany;

    @Column(name = "org_category_id")
    private Long orgCategoryId;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "schedule_title", nullable = false, length = 100)
    private String title;

    @Column(name = "schedule_description", length = 255)
    private String description;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_status", nullable = false, length = 20)
    private ScheduleStatus status;


    // 일정 수정
    public void update(ScheduleReqDto scheduleReqDto) {

        ScheduleStatus scheduleStatus = this.status;

        // status null 처리
        if (scheduleReqDto.getStatus() != null) {
            scheduleStatus = ScheduleStatus.valueOf(scheduleReqDto.getStatus().toUpperCase());
        }

        this.orgCategoryId = scheduleReqDto.getOrgCategoryId() == null ? this.orgCategoryId : scheduleReqDto.getOrgCategoryId();
        this.orgId = scheduleReqDto.getOrgId() == null ? this.orgId : scheduleReqDto.getOrgId();
        this.title = scheduleReqDto.getTitle() == null ? this.title : scheduleReqDto.getTitle();
        this.description = scheduleReqDto.getDescription() ==  null ? this.description : scheduleReqDto.getDescription();
        this.startAt = scheduleReqDto.getStartAt() == null ? this.startAt : scheduleReqDto.getStartAt();
        this.endAt = scheduleReqDto.getEndAt() ==  null ? this.endAt : scheduleReqDto.getEndAt();
        this.status = scheduleStatus;
    }
}