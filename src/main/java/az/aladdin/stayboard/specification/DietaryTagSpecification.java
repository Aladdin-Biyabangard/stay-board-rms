package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.DietaryTagEntity;
import az.aladdin.stayboard.model.request.search.DietaryTagSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class DietaryTagSpecification {

    private DietaryTagSpecification() {
    }

    public static Specification<DietaryTagEntity> withCriteria(Long hotelId, DietaryTagSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.tagName() != null && !criteria.tagName().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("tagName")),
                            "%" + criteria.tagName().toLowerCase() + "%"
                    ));
                }
                if (criteria.active() != null) {
                    predicates.add(cb.equal(root.get("active"), criteria.active()));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
