package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.SaleUnitType;
import az.aladdin.stayboard.model.enums.TaxType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderItemResponse(
        Long id,
        Long hotelId,
        Long orderId,
        String orderNumber,
        Long menuItemId,
        String menuItemName,
        SaleUnitType saleUnitType,
        long quantity,
        BigDecimal weightQuantity,
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal grossAmount,
        BigDecimal taxRate,
        TaxType taxType,
        OrderItemStatus orderItemStatus,
        List<OrderItemModifierResponse> modifiers,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        String timezone
) {
}
