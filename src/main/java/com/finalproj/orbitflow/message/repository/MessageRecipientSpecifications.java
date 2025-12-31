package com.finalproj.orbitflow.message.repository;

import com.finalproj.orbitflow.message.entity.MessageRecipient;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import com.finalproj.orbitflow.message.enums.MessageSearchType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageRecipientSpecifications {

    private MessageRecipientSpecifications() {}

    /** 받은/보낸 메시지함 검색용 */
    public static Specification<MessageRecipient> listSpec(
            Long companyId,
            Long employeeId,
            MessageFolderType folderType,
            Instant startInstant,
            Instant endExclusiveInstant,
            MessageSearchType searchType,
            String keyword
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 기본 조건
            predicates.add(cb.equal(root.get("companyId"), companyId));
            predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("isArchived"), false));
            predicates.add(cb.equal(root.get("messageFolderType"), folderType));

            // 기간 조건
            if (startInstant != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startInstant));
            }
            if (endExclusiveInstant != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endExclusiveInstant));
            }

            // 검색 조건
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                Predicate titleLike = cb.like(root.get("message").get("messageTitle"), like);
                Predicate contentLike = cb.like(root.get("message").get("messageContent"), like);
                Predicate senderLike = cb.like(root.get("message").get("sender").get("name"), like);

                switch (searchType) {
                    case TITLE -> predicates.add(titleLike);
                    case CONTENT -> predicates.add(contentLike);
                    case SENDER -> predicates.add(senderLike);
                    case ALL -> predicates.add(cb.or(titleLike, contentLike, senderLike));
                    default -> {
                        // 다른 타입은 무시
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** 보관함 검색용 */
    public static Specification<MessageRecipient> archiveSpec(
            Long companyId,
            Long employeeId,
            Instant startInstant,
            Instant endExclusiveInstant,
            MessageSearchType searchType,
            String keyword
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 기본 조건
            predicates.add(cb.equal(root.get("companyId"), companyId));
            predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("isArchived"), true));

            // 기간 조건
            if (startInstant != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startInstant));
            }
            if (endExclusiveInstant != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endExclusiveInstant));
            }

            // 검색 조건
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                Predicate titleLike = cb.like(root.get("message").get("messageTitle"), like);
                Predicate contentLike = cb.like(root.get("message").get("messageContent"), like);
                Predicate senderLike = cb.like(root.get("message").get("sender").get("name"), like);

                // RECIPIENT 검색은 서브쿼리 필요
                // 일단 ALL에 포함하지 않고 별도 처리
                switch (searchType) {
                    case TITLE -> predicates.add(titleLike);
                    case CONTENT -> predicates.add(contentLike);
                    case SENDER -> predicates.add(senderLike);
                    case RECIPIENT -> {
                        // 수신자 검색: 같은 메시지의 INBOX 수신자 중 이름이 일치하는 경우
                        // 이 경우는 복잡하므로 일단 제목/내용/발신자만 검색
                        // RECIPIENT는 나중에 구현하거나, 서브쿼리로 처리
                        predicates.add(titleLike); // 임시로 제목 검색
                    }
                    case ALL -> predicates.add(cb.or(titleLike, contentLike, senderLike));
                    default -> {
                        // 다른 타입은 무시
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

