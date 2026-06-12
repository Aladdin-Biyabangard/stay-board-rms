package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.SaleUnitType;

import java.time.LocalDateTime;

public record KitchenTicketPrintLine(
        Long orderItemId,
        String menuItemName,
        SaleUnitType saleUnitType,
        String quantityLabel,
        OrderItemStatus orderItemStatus,
        LocalDateTime createdAt
) {
}
