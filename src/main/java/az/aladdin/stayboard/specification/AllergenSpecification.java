package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.AllergenEntity;
import az.aladdin.stayboard.model.request.search.AllergenSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class AllergenSpecification {

    private AllergenSpecification() {
    }

    public static Specification<AllergenEntity> withCriteria(Long hotelId, AllergenSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.allergenName() != null && !criteria.allergenName().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("allergenName")),
                            "%" + criteria.allergenName().toLowerCase() + "%"
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
