package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.request.search.KitchenTicketSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class KitchenTicketSpecification {

    private static final List<OrderItemStatus> KITCHEN_STATUSES = List.of(
            OrderItemStatus.ORDERED,
            OrderItemStatus.PREPARING,
            OrderItemStatus.READY
    );

    private KitchenTicketSpecification() {
    }

    public static Specification<OrderItemEntity> withCriteria(Long hotelId, KitchenTicketSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));
            predicates.add(root.get("orderItemStatus").in(KITCHEN_STATUSES));

            if (criteria != null) {
                if (criteria.orderItemStatus() != null) {
                    predicates.add(cb.equal(root.get("orderItemStatus"), criteria.orderItemStatus()));
                }
                if (criteria.orderId() != null) {
                    predicates.add(cb.equal(root.get("order").get("id"), criteria.orderId()));
                }
                if (criteria.tableId() != null) {
                    predicates.add(cb.equal(root.get("order").get("tableEntity").get("id"), criteria.tableId()));
                }
                if (criteria.orderNumber() != null && !criteria.orderNumber().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("order").get("orderNumber")),
                            "%" + criteria.orderNumber().toLowerCase() + "%"
                    ));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
