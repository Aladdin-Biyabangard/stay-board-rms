package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.InventoryUnitType;
import az.aladdin.stayboard.model.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateInventoryItemRequest(
        @NotBlank String name,
        String sku,
        String description,
        Boolean active,
        @NotNull InventoryUnitType unitType,
        @NotNull UnitOfMeasure unitOfMeasure,
        @PositiveOrZero BigDecimal currentStock,
        @PositiveOrZero BigDecimal reorderLevel
) {
}
