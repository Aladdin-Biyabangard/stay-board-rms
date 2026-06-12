package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.TaxType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderItemRequest(
        @NotNull Long orderId,
        @NotNull Long menuItemId,
        @Positive long quantity,
        @PositiveOrZero BigDecimal weightQuantity,
        @NotNull @PositiveOrZero BigDecimal netAmount,
        @PositiveOrZero BigDecimal taxAmount,
        @NotNull @PositiveOrZero BigDecimal grossAmount,
        BigDecimal taxRate,
        TaxType taxType,
        @NotNull OrderItemStatus orderItemStatus,
        List<Long> modifierGroupIds
) {
}
