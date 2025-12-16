package com.finalproj.orbitflow.approval.formTemplateGroup.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : FormTemplateGroup
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "form_template_group",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_form_template_group_company_name",
                        columnNames = {"company_id", "name"}
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}