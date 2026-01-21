package com.finalproj.orbitflow.board.boardpost.repository;

import com.finalproj.orbitflow.board.boardpost.entity.BoardPost;
import com.finalproj.orbitflow.board.enums.BoardSearchType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BoardPostSpecifications {

    private BoardPostSpecifications() {
    }

    public static Specification<BoardPost> listSpec(
            Long categoryId,
            Instant startInstant,
            Instant endExclusiveInstant,
            BoardSearchType searchType,
            String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // categoryId 필터
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));

            // deleted_at null (soft delete)
            predicates.add(cb.isNull(root.get("deletedAt")));

            // 날짜 범위
            if (startInstant != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startInstant));
            }
            if (endExclusiveInstant != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endExclusiveInstant));
            }

            // 키워드
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";

                Predicate titleLike = cb.like(root.get("boardTitle"), like);
                Predicate contentLike = cb.like(root.get("boardContent"), like);
                Predicate authorLike = cb.like(root.get("writer").get("name"), like);

                switch (searchType) {
                    case TITLE -> predicates.add(titleLike);
                    case CONTENT -> predicates.add(contentLike);
                    case AUTHOR -> predicates.add(authorLike);
                    case ALL -> predicates.add(cb.or(titleLike, contentLike, authorLike));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
