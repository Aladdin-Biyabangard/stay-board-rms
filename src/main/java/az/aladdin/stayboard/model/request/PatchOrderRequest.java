package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.OrderStatus;

import java.math.BigDecimal;

public record PatchOrderRequest(
        String orderNumber,
        GuestInformationRequest guestInformation,
        Long tableId,
        String roomNumber,
        BigDecimal totalAmount,
        OrderStatus orderStatus
) {
}
