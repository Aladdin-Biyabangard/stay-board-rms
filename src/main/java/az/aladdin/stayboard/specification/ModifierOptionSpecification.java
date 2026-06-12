package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.ModifierOptionEntity;
import az.aladdin.stayboard.model.request.search.ModifierOptionSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ModifierOptionSpecification {

    private ModifierOptionSpecification() {
    }

    public static Specification<ModifierOptionEntity> withCriteria(Long hotelId, ModifierOptionSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.modifierGroupId() != null) {
                    predicates.add(cb.equal(root.get("modifierGroup").get("id"), criteria.modifierGroupId()));
                }
                if (criteria.optionName() != null && !criteria.optionName().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("optionName")),
                            "%" + criteria.optionName().toLowerCase() + "%"
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
