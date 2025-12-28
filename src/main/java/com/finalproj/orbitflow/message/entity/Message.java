package com.finalproj.orbitflow.message.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "message")
public class Message extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    /** 발신자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee sender;

    @Column(name = "message_title", nullable = false, length = 255)
    private String messageTitle;

    @Lob
    @Column(name = "message_content", nullable = false)
    private String messageContent;

    /**
     * 파일은 "자리만" (나중에 File 엔티티 연관으로 바꿔도 됨)
     * - 지금은 file_id 컬럼만 관리
     */
    @Column(name = "file_id")
    private Long fileId;

}