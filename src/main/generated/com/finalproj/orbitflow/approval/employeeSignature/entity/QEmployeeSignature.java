package com.finalproj.orbitflow.global.image.signature.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEmployeeSignature is a Querydsl query type for EmployeeSignature
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmployeeSignature extends EntityPathBase<EmployeeSignature> {

    private static final long serialVersionUID = -1098424368L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEmployeeSignature employeeSignature = new QEmployeeSignature("signature");

    public final com.finalproj.orbitflow.hr.company.entity.QCompany company;

    public final com.finalproj.orbitflow.hr.employee.entity.QEmployee employee;

    public final com.finalproj.orbitflow.global.file.entity.QFile file;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public QEmployeeSignature(String variable) {
        this(EmployeeSignature.class, forVariable(variable), INITS);
    }

    public QEmployeeSignature(Path<? extends EmployeeSignature> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEmployeeSignature(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEmployeeSignature(PathMetadata metadata, PathInits inits) {
        this(EmployeeSignature.class, metadata, inits);
    }

    public QEmployeeSignature(Class<? extends EmployeeSignature> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.company = inits.isInitialized("company") ? new com.finalproj.orbitflow.hr.company.entity.QCompany(forProperty("company")) : null;
        this.employee = inits.isInitialized("employee") ? new com.finalproj.orbitflow.hr.employee.entity.QEmployee(forProperty("employee"), inits.get("employee")) : null;
        this.file = inits.isInitialized("file") ? new com.finalproj.orbitflow.global.file.entity.QFile(forProperty("file"), inits.get("file")) : null;
    }

}

