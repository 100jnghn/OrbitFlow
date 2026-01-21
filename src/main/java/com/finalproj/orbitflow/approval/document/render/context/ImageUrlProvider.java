package com.finalproj.orbitflow.approval.document.render.context;

/**
 * 문서에 포함된 이미지에 접근할 수 있는 URL을 생성하기 위한 인터페이스.
 * <p>
 * 문서 ID와 파일 ID를 기준으로,
 * 외부에서 접근 가능한 이미지 URL을 만들어주는 역할을 한다.
 * <p>
 * 실제 URL 생성 방식은 사용하는 컨텍스트에 따라 달라질 수 있으며,
 * 이 인터페이스는 그 차이를 숨기기 위한 용도로 사용된다.
 *
 * @author : Choi MinHyeok
 * @filename : ImageUrlProvider
 * @since : 26. 1. 4. 일요일
 */

@FunctionalInterface
public interface ImageUrlProvider {
    String generate(Long documentId, Long targetId);
}