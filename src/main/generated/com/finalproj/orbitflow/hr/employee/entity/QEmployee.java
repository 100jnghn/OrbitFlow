package com.finalproj.orbitflow.hr.employee.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEmployee is a Querydsl query type for Employee
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmployee extends EntityPathBase<Employee> {

    private static final long serialVersionUID = 573714143L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEmployee employee = new QEmployee("employee");

    public final com.finalproj.orbitflow.global.common.QBaseEntity _super = new com.finalproj.orbitflow.global.common.QBaseEntity(this);

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final StringPath email = createString("email");

    public final StringPath employeeNo = createString("employeeNo");

    public final EnumPath<com.finalproj.orbitflow.hr.employee.enums.EmploymentType> employmentType = createEnum("employmentType", com.finalproj.orbitflow.hr.employee.enums.EmploymentType.class);

    public final EnumPath<com.finalproj.orbitflow.hr.employee.enums.Gender> gender = createEnum("gender", com.finalproj.orbitflow.hr.employee.enums.Gender.class);

    public final DatePath<java.time.LocalDate> hireDate = createDate("hireDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath internalPhone = createString("internalPhone");

    //inherited
    public final NumberPath<Long> modifiedBy = _super.modifiedBy;

    public final StringPath name = createString("name");

    public final com.finalproj.orbitflow.hr.organization.entity.QOrganization organization;

    public final StringPath password = createString("password");

    public final StringPath phone = createString("phone");

    public final com.finalproj.orbitflow.hr.positionCategory.entity.QPositionCategory positionCategory;

    public final com.finalproj.orbitflow.hr.rank.entity.QHrRank rank;

    public final EnumPath<com.finalproj.orbitflow.hr.employee.enums.EmployeeRole> role = createEnum("role", com.finalproj.orbitflow.hr.employee.enums.EmployeeRole.class);

    public final EnumPath<com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus> status = createEnum("status", com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public final EnumPath<com.finalproj.orbitflow.hr.employee.enums.WorkStatus> workStatus = createEnum("workStatus", com.finalproj.orbitflow.hr.employee.enums.WorkStatus.class);

    public QEmployee(String variable) {
        this(Employee.class, forVariable(variable), INITS);
    }

    public QEmployee(Path<? extends Employee> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEmployee(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEmployee(PathMetadata metadata, PathInits inits) {
        this(Employee.class, metadata, inits);
    }

    public QEmployee(Class<? extends Employee> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.organization = inits.isInitialized("organization") ? new com.finalproj.orbitflow.hr.organization.entity.QOrganization(forProperty("organization")) : null;
        this.positionCategory = inits.isInitialized("positionCategory") ? new com.finalproj.orbitflow.hr.positionCategory.entity.QPositionCategory(forProperty("positionCategory"), inits.get("positionCategory")) : null;
        this.rank = inits.isInitialized("rank") ? new com.finalproj.orbitflow.hr.rank.entity.QHrRank(forProperty("rank"), inits.get("rank")) : null;
    }

}

