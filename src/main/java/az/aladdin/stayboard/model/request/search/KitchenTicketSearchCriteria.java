package az.aladdin.stayboard.model.request.search;

import az.aladdin.stayboard.model.enums.OrderItemStatus;

public record KitchenTicketSearchCriteria(
        OrderItemStatus orderItemStatus,
        Long orderId,
        Long tableId,
        String orderNumber
) {
}
