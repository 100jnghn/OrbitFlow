package com.finalproj.orbitflow.approval.document.content.entity;



import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결재 문서의 실제 작성 내용을 JSON 형태로 저장하는 엔티티.
 * <p>
 * 하나의 Document와 1:1 관계를 가지며,
 * 사용자가 문서 작성 화면에서 입력한 모든 값은
 * contentJson 컬럼에 그대로 저장된다.
 * <p>
 * 결재 반려 후 재작성 시에는 기존 문서의 내용을 복사하여
 * 새로운 문서에 연결된 DocumentContent를 생성하는 용도로도 사용된다.
 * <p>
 * 문서 내용의 구조나 의미 해석은 담당하지 않으며,
 * 내용 관리의 책임은 상위 서비스 레이어에 위임한다.
 *
 * @filename    : DocumentContent
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */


@Entity
@Table(name = "document_content")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(name = "content_json", nullable = false, columnDefinition = "json")
    private String contentJson;

    public void updateContentJson(String updateJson) {
        this.contentJson = updateJson;
    }

    public static DocumentContent create(Document document, String contentJson) {
        return DocumentContent.builder()
                .document(document)
                .contentJson(contentJson)
                .build();
    }

    public static DocumentContent revise(Document revised, DocumentContent rejectedContent) {
        return DocumentContent.builder()
                .document(revised)
                .contentJson(rejectedContent.contentJson)
                .build();
    }
}
