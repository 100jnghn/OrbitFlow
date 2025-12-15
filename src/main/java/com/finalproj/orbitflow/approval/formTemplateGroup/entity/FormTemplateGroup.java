package com.finalproj.orbitflow.approval.formTemplateGroup.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : FormTemplateGroup
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "form_template_group")
@Getter
@NoArgsConstructor
public class FormTemplateGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            insertable = false,
            updatable = false
    )//읽기 전용. 수정 불가. 삽입 불가.
    private Employee creator;
}