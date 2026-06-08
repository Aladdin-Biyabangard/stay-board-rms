package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.model.request.search.OrderSearchCriteria;
import az.aladdin.stayboard.security.GuestOrderAccess;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<OrderEntity> withCriteria(Long hotelId, OrderSearchCriteria criteria) {
        return withCriteria(hotelId, criteria, null);
    }

    public static Specification<OrderEntity> withCriteria(
            Long hotelId,
            OrderSearchCriteria criteria,
            GuestOrderAccess.Scope guestScope
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (guestScope != null) {
                predicates.add(GuestOrderAccess.ownershipPredicate(
                        cb,
                        root.get("guestInformation"),
                        guestScope
                ));
            }

            if (criteria != null) {
                if (criteria.orderNumber() != null && !criteria.orderNumber().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("orderNumber")),
                            "%" + criteria.orderNumber().toLowerCase() + "%"
                    ));
                }
                if (criteria.tableId() != null) {
                    predicates.add(cb.equal(root.get("tableEntity").get("id"), criteria.tableId()));
                }
                if (criteria.roomNumber() != null && !criteria.roomNumber().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("roomNumber")),
                            "%" + criteria.roomNumber().toLowerCase() + "%"
                    ));
                }
                if (criteria.orderStatus() != null) {
                    predicates.add(cb.equal(root.get("orderStatus"), criteria.orderStatus()));
                }
                if (criteria.guestFirstName() != null && !criteria.guestFirstName().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("guestInformation").get("guestFirstName")),
                            "%" + criteria.guestFirstName().toLowerCase() + "%"
                    ));
                }
                if (criteria.guestLastName() != null && !criteria.guestLastName().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("guestInformation").get("guestLastName")),
                            "%" + criteria.guestLastName().toLowerCase() + "%"
                    ));
                }
                if (criteria.createdFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.createdFrom()));
                }
                if (criteria.createdTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.createdTo()));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
