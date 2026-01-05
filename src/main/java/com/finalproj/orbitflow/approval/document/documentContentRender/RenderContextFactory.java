package com.finalproj.orbitflow.approval.document.documentContentRender;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : RenderContextFactory
 * @since : 26. 1. 4. 일요일
 **/


@Component
@RequiredArgsConstructor
public class RenderContextFactory {

    private final ImageUrlProvider imageUrlProvider;
    private final VacationTypeNameResolver vacationTypeNameResolver;

    public RenderContext create(Long documentId) {
        return new RenderContext(
                documentId,
                imageUrlProvider,
                vacationTypeNameResolver
        );
    }
}
