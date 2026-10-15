package com.tekna.api.specification;

import com.tekna.api.dto.ProductFilterInput;
import com.tekna.api.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> withFilter(ProductFilterInput filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().trim().toLowerCase() + "%"));
            }

            if (filter.getSku() != null && !filter.getSku().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("sku")), "%" + filter.getSku().trim().toLowerCase() + "%"));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (Boolean.TRUE.equals(filter.getLowStockOnly())) {
                predicates.add(cb.lessThanOrEqualTo(root.get("stock"), root.get("minStock")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
