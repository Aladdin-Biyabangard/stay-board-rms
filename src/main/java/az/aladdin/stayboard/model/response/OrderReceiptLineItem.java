package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.SaleUnitType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderReceiptLineItem(
        Long orderItemId,
        String menuItemName,
        SaleUnitType saleUnitType,
        String quantityLabel,
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal grossAmount,
        OrderItemStatus orderItemStatus,
        LocalDateTime createdAt
) {
}
