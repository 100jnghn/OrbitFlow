package com.finalproj.orbitflow.approval.form.template.category.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : TemplateCategory
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


import com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "template_category")
@Getter
@NoArgsConstructor
public class TemplateCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false, unique = true)
    private TemplateCategoryCode code;

    @Column(length = 100, nullable = false)
    private String name;
}
