package com.finalproj.orbitflow.message.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "message")
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
     * 첨부파일 목록 (일대다 관계)
     * - File 엔티티에 message_id 컬럼이 필요함 (File 엔티티 수정 없이 조인 컬럼 방식으로 처리 시도)
     * - 하지만 File 엔티티가 공용이라 message_id 필드가 없을 수 있음.
     * - @JoinColumn(name = "message_id")을 사용하면 File 테이블에 message_id 컬럼이 있어야 함.
     * - File 엔티티는 공용이므로 특정 도메인 컬럼을 추가하기 부담스러울 수 있으나,
     * - Board에서도 @JoinColumn(name = "board_id")를 사용하므로, Message도 동일한 패턴을 따름.
     * - (실제 DB에 message_id 컬럼이 추가되어야 함)
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "message_id")
    private List<File> files;

}