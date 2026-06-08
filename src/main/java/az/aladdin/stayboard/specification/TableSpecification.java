package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.model.request.search.TableSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TableSpecification {

    private TableSpecification() {
    }

    public static Specification<TableEntity> withCriteria(Long hotelId, TableSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.tableNumber() != null && !criteria.tableNumber().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("tableNumber")),
                            "%" + criteria.tableNumber().toLowerCase() + "%"
                    ));
                }
                if (criteria.minCapacity() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("capacity"), criteria.minCapacity()));
                }
                if (criteria.maxCapacity() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("capacity"), criteria.maxCapacity()));
                }
                if (criteria.mergeable() != null) {
                    predicates.add(cb.equal(root.get("mergeable"), criteria.mergeable()));
                }
                if (criteria.merged() != null) {
                    if (criteria.merged()) {
                        predicates.add(cb.isNotNull(root.get("mergeGroupId")));
                    } else {
                        predicates.add(cb.isNull(root.get("mergeGroupId")));
                    }
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
