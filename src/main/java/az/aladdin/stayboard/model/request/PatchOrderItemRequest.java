package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.TaxType;

import java.math.BigDecimal;

public record PatchOrderItemRequest(
        Long orderId,
        Long menuItemId,
        Long quantity,
        BigDecimal weightQuantity,
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal grossAmount,
        BigDecimal taxRate,
        TaxType taxType,
        OrderItemStatus orderItemStatus
) {
}
