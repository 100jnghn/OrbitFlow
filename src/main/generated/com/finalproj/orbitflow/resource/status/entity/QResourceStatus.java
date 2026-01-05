package com.finalproj.orbitflow.resource.status.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QResourceStatus is a Querydsl query type for ResourceStatus
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResourceStatus extends EntityPathBase<ResourceStatus> {

    private static final long serialVersionUID = -1905595695L;

    public static final QResourceStatus resourceStatus = new QResourceStatus("resourceStatus");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.finalproj.orbitflow.resource.enums.ResourceStatusCode> resourceStatusCode = createEnum("resourceStatusCode", com.finalproj.orbitflow.resource.enums.ResourceStatusCode.class);

    public final StringPath statusName = createString("statusName");

    public QResourceStatus(String variable) {
        super(ResourceStatus.class, forVariable(variable));
    }

    public QResourceStatus(Path<? extends ResourceStatus> path) {
        super(path.getType(), path.getMetadata());
    }

    public QResourceStatus(PathMetadata metadata) {
        super(ResourceStatus.class, metadata);
    }

}

