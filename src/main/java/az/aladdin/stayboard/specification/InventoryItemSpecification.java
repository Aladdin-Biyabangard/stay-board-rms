package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.InventoryItemEntity;
import az.aladdin.stayboard.model.request.search.InventoryItemSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class InventoryItemSpecification {

    private InventoryItemSpecification() {
    }

    public static Specification<InventoryItemEntity> withCriteria(Long hotelId, InventoryItemSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.name() != null && !criteria.name().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.name().toLowerCase() + "%"));
                }
                if (criteria.sku() != null && !criteria.sku().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("sku")), "%" + criteria.sku().toLowerCase() + "%"));
                }
                if (criteria.active() != null) {
                    predicates.add(cb.equal(root.get("active"), criteria.active()));
                }
                if (criteria.unitType() != null) {
                    predicates.add(cb.equal(root.get("unitType"), criteria.unitType()));
                }
                if (Boolean.TRUE.equals(criteria.lowStock())) {
                    predicates.add(cb.isNotNull(root.get("reorderLevel")));
                    predicates.add(cb.lessThanOrEqualTo(root.get("currentStock"), root.get("reorderLevel")));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
