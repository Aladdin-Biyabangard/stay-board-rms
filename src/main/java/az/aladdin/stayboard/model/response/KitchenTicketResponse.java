package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.InventoryUnitType;
import az.aladdin.stayboard.model.enums.OrderItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KitchenTicketResponse(
        Long orderItemId,
        Long orderId,
        String orderNumber,
        Long tableId,
        String tableNumber,
        String roomNumber,
        Long menuItemId,
        String menuItemName,
        InventoryUnitType saleUnitType,
        long quantity,
        BigDecimal weightQuantity,
        OrderItemStatus orderItemStatus,
        LocalDateTime createdAt
) {
}
