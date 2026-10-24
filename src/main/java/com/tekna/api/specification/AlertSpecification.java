package com.tekna.api.specification;

import com.tekna.api.dto.AlertFilterInput;
import com.tekna.api.entity.Alert;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class AlertSpecification {

    private AlertSpecification() {
    }

    public static Specification<Alert> withFilter(AlertFilterInput filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getProductQuery() != null && !filter.getProductQuery().trim().isEmpty()) {
                String pattern = "%" + filter.getProductQuery().trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("product").get("name")), pattern);
                Predicate skuMatch = cb.like(cb.lower(root.get("product").get("sku")), pattern);
                predicates.add(cb.or(nameMatch, skuMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
