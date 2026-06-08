package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.model.request.search.MenuItemSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class MenuItemSpecification {

    private MenuItemSpecification() {
    }

    public static Specification<MenuItemEntity> withCriteria(Long hotelId, MenuItemSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.itemName() != null && !criteria.itemName().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("itemName")),
                            "%" + criteria.itemName().toLowerCase() + "%"
                    ));
                }
                if (criteria.active() != null) {
                    predicates.add(cb.equal(root.get("active"), criteria.active()));
                }
                if (criteria.menuCategoryId() != null) {
                    predicates.add(cb.equal(root.get("menuCategory").get("id"), criteria.menuCategoryId()));
                }
                if (criteria.taxType() != null) {
                    predicates.add(cb.equal(root.get("taxType"), criteria.taxType()));
                }
                if (criteria.minPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
                }
                if (criteria.maxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
