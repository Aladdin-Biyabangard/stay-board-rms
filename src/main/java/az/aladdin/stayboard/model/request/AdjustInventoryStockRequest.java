package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.InventoryTransactionType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AdjustInventoryStockRequest(
        @NotNull BigDecimal quantityDelta,
        @NotNull InventoryTransactionType transactionType,
        String notes
) {
}
