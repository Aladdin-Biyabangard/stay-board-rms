package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.RecipeEntity;
import az.aladdin.stayboard.model.request.search.RecipeSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class RecipeSpecification {

    private RecipeSpecification() {
    }

    public static Specification<RecipeEntity> withCriteria(Long hotelId, RecipeSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.menuItemId() != null) {
                    predicates.add(cb.equal(root.get("menuItem").get("id"), criteria.menuItemId()));
                }
                if (criteria.inventoryItemId() != null) {
                    predicates.add(cb.equal(root.get("inventoryItem").get("id"), criteria.inventoryItemId()));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
