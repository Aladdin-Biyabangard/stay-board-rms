package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.model.request.search.OrderItemSearchCriteria;
import az.aladdin.stayboard.security.GuestOrderAccess;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class OrderItemSpecification {

    private OrderItemSpecification() {
    }

    public static Specification<OrderItemEntity> withCriteria(Long hotelId, OrderItemSearchCriteria criteria) {
        return withCriteria(hotelId, criteria, null);
    }

    public static Specification<OrderItemEntity> withCriteria(
            Long hotelId,
            OrderItemSearchCriteria criteria,
            GuestOrderAccess.Scope guestScope
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (guestScope != null) {
                var orderJoin = root.join("order", JoinType.INNER);
                predicates.add(GuestOrderAccess.ownershipPredicate(
                        cb,
                        orderJoin.get("guestInformation"),
                        guestScope
                ));
            }

            if (criteria != null) {
                if (criteria.orderId() != null) {
                    predicates.add(cb.equal(root.get("order").get("id"), criteria.orderId()));
                }
                if (criteria.menuItemId() != null) {
                    predicates.add(cb.equal(root.get("menuItem").get("id"), criteria.menuItemId()));
                }
                if (criteria.orderItemStatus() != null) {
                    predicates.add(cb.equal(root.get("orderItemStatus"), criteria.orderItemStatus()));
                }
                if (criteria.taxType() != null) {
                    predicates.add(cb.equal(root.get("taxType"), criteria.taxType()));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
