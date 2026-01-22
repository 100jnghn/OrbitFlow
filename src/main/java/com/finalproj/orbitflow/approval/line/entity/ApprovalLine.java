package com.finalproj.orbitflow.approval.line.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : ApprovalLine
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.line.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "approval_line",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_al_document_order",
                        columnNames = {"document_id", "order_no"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_org_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_position_category_id")
    private PositionCategory positionCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Employee approver;

    @Column(name = "order_no", nullable = false)
    private int orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    public void setApprover(Employee head) {
        this.approver = head;
    }

    public void markWaiting() {
        this.status = ApprovalStatus.WAITING;
    }

    public void markInProgress() {
        this.status = ApprovalStatus.IN_PROGRESS;
    }

    public void markCancelled() {
        this.status = ApprovalStatus.CANCELLED;
    }

    public void markApproved(String comment) {
        this.status = ApprovalStatus.APPROVED;
        this.comment = comment;
    }

    public void changeOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }


    public void reject(String comment) {
        this.comment = comment;
        this.status = ApprovalStatus.REJECTED;
    }
}
