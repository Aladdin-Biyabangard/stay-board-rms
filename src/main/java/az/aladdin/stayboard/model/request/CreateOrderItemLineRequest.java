package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/** Line item payload when creating an order with items in a single request (no orderId). */
public record CreateOrderItemLineRequest(
        @NotNull Long menuItemId,
        @Positive long quantity,
        @PositiveOrZero BigDecimal weightQuantity,
        List<Long> modifierGroupIds,
        List<Long> modifierOptionIds
) {
}
