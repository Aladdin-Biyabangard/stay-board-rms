package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.TableOccupancyEntity;
import az.aladdin.stayboard.model.request.search.TableOccupancySearchCriteria;
import az.aladdin.stayboard.security.GuestOrderAccess;
import az.aladdin.stayboard.security.GuestTableAccess;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TableOccupancySpecification {

    private TableOccupancySpecification() {
    }

    public static Specification<TableOccupancyEntity> withCriteria(
            Long hotelId,
            TableOccupancySearchCriteria criteria,
            GuestOrderAccess.Scope guestScope
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.tableId() != null) {
                    predicates.add(cb.equal(root.get("restaurantTable").get("id"), criteria.tableId()));
                }
                if (criteria.sourceType() != null) {
                    predicates.add(cb.equal(root.get("sourceType"), criteria.sourceType()));
                }
                if (criteria.from() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("endDateTime"), criteria.from()));
                }
                if (criteria.to() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("startDateTime"), criteria.to()));
                }
                if (Boolean.TRUE.equals(criteria.mineOnly()) && guestScope != null) {
                    predicates.add(GuestTableAccess.ownershipPredicate(cb, root.get("reservationMainInfo"), guestScope));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
