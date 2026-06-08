package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.TaxType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderItemResponse(
        Long id,
        Long hotelId,
        Long orderId,
        String orderNumber,
        Long menuItemId,
        String menuItemName,
        long quantity,
        BigDecimal weightQuantity,
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal grossAmount,
        BigDecimal taxRate,
        TaxType taxType,
        OrderItemStatus orderItemStatus,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        String timezone
) {
}
