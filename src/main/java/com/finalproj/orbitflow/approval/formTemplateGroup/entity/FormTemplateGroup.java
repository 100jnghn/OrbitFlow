package com.finalproj.orbitflow.approval.formTemplateGroup.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : FormTemplateGroup
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.approval.templateCategory.entity.TemplateCategory;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormTemplateGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_category_id", nullable = false)
    private TemplateCategory templateCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_role", length = 30)
    private BaseRole baseRole;               // ✅ 추가 (nullable)


    @Column(nullable = false)
    private Boolean active;


    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
