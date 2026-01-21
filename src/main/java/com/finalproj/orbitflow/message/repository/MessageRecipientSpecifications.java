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

    private MessageRecipientSpecifications() {
    }

    /** 받은/보낸 메시지함 검색용 */
    public static Specification<MessageRecipient> listSpec(
            Long companyId,
            Long employeeId,
            MessageFolderType folderType,
            Instant startInstant,
            Instant endExclusiveInstant,
            MessageSearchType searchType,
            String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 기본 필터: 회사 ID
            predicates.add(cb.equal(root.get("companyId"), companyId));

            // 페치 조인 (N+1 방지) - 카운트 쿼리가 아닐 때만 수행
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("employee", jakarta.persistence.criteria.JoinType.LEFT);
                jakarta.persistence.criteria.Fetch<Object, Object> messageFetch = root.fetch("message",
                        jakarta.persistence.criteria.JoinType.LEFT);
                messageFetch.fetch("sender", jakarta.persistence.criteria.JoinType.LEFT);
                messageFetch.fetch("files", jakarta.persistence.criteria.JoinType.LEFT);
            }

            if (folderType == MessageFolderType.SENT) {
                // [SENT 폴더 조회 변경]
                // 1. 본인의 SENT 레코드를 직접 조회
                predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
                predicates.add(cb.equal(root.get("messageFolderType"), MessageFolderType.SENT));
                predicates.add(cb.isNull(root.get("deletedAt")));
                predicates.add(cb.equal(root.get("isArchived"), false));
            } else {
                // [INBOX 등 일반 폴더 조회]
                predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
                predicates.add(cb.equal(root.get("messageFolderType"), folderType));
                predicates.add(cb.isNull(root.get("deletedAt")));
                predicates.add(cb.equal(root.get("isArchived"), false));
            }

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

                Predicate recipientLike;
                // 보낸메시지함 등에서는 서브쿼리로 해당 메시지의 모든 수신자를 확인
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<MessageRecipient> subRoot = subquery.from(MessageRecipient.class);
                subquery.select(cb.literal(1L));
                subquery.where(
                        cb.equal(subRoot.get("message"), root.get("message")),
                        cb.equal(subRoot.get("messageFolderType"), MessageFolderType.INBOX),
                        cb.like(subRoot.get("employee").get("name"), like));
                recipientLike = cb.exists(subquery);

                switch (searchType) {
                    case TITLE -> predicates.add(titleLike);
                    case CONTENT -> predicates.add(contentLike);
                    case SENDER -> predicates.add(cb.like(root.get("message").get("sender").get("name"), like));
                    case RECIPIENT -> predicates.add(recipientLike);
                    case ALL -> {
                        if (folderType == MessageFolderType.SENT) {
                            // 보낸 메시지함에서는 본인이 발신자이므로 sender 대신 recipient 검색 포함
                            predicates.add(cb.or(titleLike, contentLike, recipientLike));
                        } else {
                            // 받은 메시지함에서는 제목, 내용, 발신자 검색
                            Predicate senderLike = cb.like(root.get("message").get("sender").get("name"), like);
                            predicates.add(cb.or(titleLike, contentLike, senderLike));
                        }
                    }
                    default -> {
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
            String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 기본 필터: 회사 ID 및 본인 여부
            predicates.add(cb.equal(root.get("companyId"), companyId));
            predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("isArchived"), true));

            // 페치 조인 (N+1 방지)
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("employee", jakarta.persistence.criteria.JoinType.LEFT);
                jakarta.persistence.criteria.Fetch<Object, Object> messageFetch = root.fetch("message",
                        jakarta.persistence.criteria.JoinType.LEFT);
                messageFetch.fetch("sender", jakarta.persistence.criteria.JoinType.LEFT);
                messageFetch.fetch("files", jakarta.persistence.criteria.JoinType.LEFT);
            }

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

                // 수신자 검색용 서브쿼리
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<MessageRecipient> subRoot = subquery.from(MessageRecipient.class);
                subquery.select(cb.literal(1L));
                subquery.where(
                        cb.equal(subRoot.get("message"), root.get("message")),
                        cb.equal(subRoot.get("messageFolderType"), MessageFolderType.INBOX),
                        cb.like(subRoot.get("employee").get("name"), like));
                Predicate recipientLike = cb.exists(subquery);

                switch (searchType) {
                    case TITLE -> predicates.add(titleLike);
                    case CONTENT -> predicates.add(contentLike);
                    case SENDER -> predicates.add(senderLike);
                    case RECIPIENT -> predicates.add(recipientLike);
                    case ALL -> {
                        // 보관함에서는 발신자/수신자 모두 검색에 포함
                        predicates.add(cb.or(titleLike, contentLike, senderLike, recipientLike));
                    }
                    default -> {
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
