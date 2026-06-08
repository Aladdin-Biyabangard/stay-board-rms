package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long hotelId,
        String orderNumber,
        GuestInformationResponse guestInformation,
        Long tableId,
        String tableNumber,
        String roomNumber,
        BigDecimal totalAmount,
        OrderStatus orderStatus,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        String timezone
) {
}
