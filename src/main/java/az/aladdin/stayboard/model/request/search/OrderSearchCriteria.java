package az.aladdin.stayboard.model.request.search;

import az.aladdin.stayboard.model.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderSearchCriteria(
        String orderNumber,
        Long tableId,
        String roomNumber,
        OrderStatus orderStatus,
        String guestFirstName,
        String guestLastName,
        LocalDateTime createdFrom,
        LocalDateTime createdTo
) {
}
