package com.tekna.api.specification;

import com.tekna.api.dto.InventoryMovementFilterInput;
import com.tekna.api.entity.InventoryMovement;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class InventoryMovementSpecification {

    private InventoryMovementSpecification() {
    }

    public static Specification<InventoryMovement> withFilter(InventoryMovementFilterInput filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            if (filter.getSupplierId() != null) {
                predicates.add(cb.equal(root.get("supplier").get("id"), filter.getSupplierId()));
            }

            if (filter.getMovementType() != null) {
                predicates.add(cb.equal(root.get("movementType"), filter.getMovementType()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
