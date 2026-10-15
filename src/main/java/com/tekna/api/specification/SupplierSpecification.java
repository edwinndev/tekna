package com.tekna.api.specification;

import com.tekna.api.dto.SupplierFilterInput;
import com.tekna.api.entity.Supplier;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class SupplierSpecification {

    private SupplierSpecification() {
    }

    public static Specification<Supplier> withFilter(SupplierFilterInput filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().trim().toLowerCase() + "%"));
            }

            if (filter.getContactName() != null && !filter.getContactName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("contactName")), "%" + filter.getContactName().trim().toLowerCase() + "%"));
            }

            if (filter.getEmail() != null && !filter.getEmail().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + filter.getEmail().trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
