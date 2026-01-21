package com.finalproj.orbitflow.approval.document.render.support;

/**
 * 휴가 타입 식별자를 표시용 이름으로 변환하기 위한 인터페이스.
 * <p>
 * 문서 렌더링이나 PDF 출력 과정에서는 휴가 타입이
 * ID 형태로 저장·전달되기 때문에,
 * 이를 사용자에게 보여줄 문자열로 변환하는 책임을 분리하기 위해 사용된다.
 * <p>
 * 렌더링 계층은 휴가 타입의 저장 방식이나 조회 로직을 알 필요 없이
 * 이 인터페이스를 통해 일관된 방식으로 휴가 타입 이름을 얻는다.
 * <p>
 * 구현체에 따라 DB 조회, 코드 테이블, 캐시 등
 * 다양한 방식으로 휴가 타입 이름을 해석할 수 있도록 확장 가능하다.
 *
 * @author : Choi MinHyeok
 * @filename : VacationTypeNameResolver
 * @since : 26. 1. 4. 일요일
 */


public interface VacationTypeNameResolver {
    String resolve(String vacationTypeId);
}
