package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.entity.ConnectionRequest;
import com.vasudha.referral_backend.entity.ConnectionStatus;
import com.vasudha.referral_backend.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;

public class UserSpecification {

    public static Specification<User> search(
            String name,
            String skills,
            String company,
            String headline,
            Long currentUserId
    ) {

        return (Root<User> root,
                CriteriaQuery<?> query,
                CriteriaBuilder cb) -> {

            // Avoid duplicate rows if joins are introduced
            query.distinct(true);

            Predicate predicate = cb.conjunction();

            // 🔹 Name filter
            if (name != null && !name.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(
                                cb.lower(root.get("fullName")),
                                "%" + name.toLowerCase() + "%"
                        )
                );
            }

            // 🔹 Skills filter
            if (skills != null && !skills.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(
                                cb.lower(root.get("skills")),
                                "%" + skills.toLowerCase() + "%"
                        )
                );
            }

            // 🔹 Company filter
            if (company != null && !company.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(
                                cb.lower(root.get("company")),
                                "%" + company.toLowerCase() + "%"
                        )
                );
            }

            // 🔹 Headline filter
            if (headline != null && !headline.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(
                                cb.lower(root.get("headline")),
                                "%" + headline.toLowerCase() + "%"
                        )
                );
            }

            // 🔥 Exclude current user
            predicate = cb.and(predicate,
                    cb.notEqual(root.get("id"), currentUserId)
            );

            // 🔥 Exclude already connected users
            Subquery<Object> subquery = query.subquery(Object.class);
            Root<ConnectionRequest> connectionRoot = subquery.from(ConnectionRequest.class);

            subquery.select(
                    cb.selectCase()
                            .when(

                                    cb.equal(connectionRoot.get("sender").get("id"), currentUserId),
                                    connectionRoot.get("receiver").get("id")
                            )
                            .otherwise(connectionRoot.get("sender").get("id"))
            );

            subquery.where(
                    cb.and(
                            cb.or(
                                    cb.equal(connectionRoot.get("sender").get("id"), currentUserId),
                                    cb.equal(connectionRoot.get("receiver").get("id"), currentUserId)
                            ),
                            cb.equal(connectionRoot.get("status"), ConnectionStatus.ACCEPTED)
                    )
            );

            predicate = cb.and(predicate,
                    cb.not(root.get("id").in(subquery))
            );

            return predicate;
        };
    }
}
