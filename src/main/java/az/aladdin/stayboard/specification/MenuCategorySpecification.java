package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.MenuCategoryEntity;
import az.aladdin.stayboard.model.request.search.MenuCategorySearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class MenuCategorySpecification {

    private MenuCategorySpecification() {
    }

    public static Specification<MenuCategoryEntity> withCriteria(Long hotelId, MenuCategorySearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.categoryName() != null && !criteria.categoryName().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("categoryName")),
                            "%" + criteria.categoryName().toLowerCase() + "%"
                    ));
                }
                if (criteria.description() != null && !criteria.description().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("description")),
                            "%" + criteria.description().toLowerCase() + "%"
                    ));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
