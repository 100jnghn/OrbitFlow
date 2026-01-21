package com.finalproj.orbitflow.approval.document.render.context;

import com.finalproj.orbitflow.approval.document.render.support.VacationTypeNameResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PDF 렌더링에 사용할 RenderContext를 생성하는 팩토리.
 * <p>
 * 렌더링 과정에서 필요한 공통 의존성(ImageUrlProvider, 도메인 resolver 등)을
 * 한곳에서 묶어 RenderContext로 만들어준다.
 * <p>
 * Renderer나 상위 서비스가 개별 의존성을 직접 조립하지 않도록,
 * RenderContext 생성 책임을 이 클래스로 분리했다.
 *
 * @author : Choi MinHyeok
 * @filename : RenderContextFactory
 * @since : 26. 1. 4. 일요일
 */


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
