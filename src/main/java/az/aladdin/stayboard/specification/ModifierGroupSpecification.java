package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.ModifierGroupEntity;
import az.aladdin.stayboard.model.request.search.ModifierGroupSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ModifierGroupSpecification {

    private ModifierGroupSpecification() {
    }

    public static Specification<ModifierGroupEntity> withCriteria(Long hotelId, ModifierGroupSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.groupName() != null && !criteria.groupName().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("groupName")),
                            "%" + criteria.groupName().toLowerCase() + "%"
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
