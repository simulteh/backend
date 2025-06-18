package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.entities.User;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class UserSpecifications {
    public static Specification<User> filterBy(String username, String email, String createdAt) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (username != null) {
                predicates = cb.and(predicates, cb.like(root.get("username"), "%" + username + "%"));
            }
            if (email != null) {
                predicates = cb.and(predicates, cb.like(root.get("email"), "%" + email + "%"));
            }
            if (createdAt != null) {
                predicates = cb.and(predicates, cb.equal(root.get("createdAt"), LocalDateTime.parse(createdAt)));
            }
            return predicates;
        };
    }
} 