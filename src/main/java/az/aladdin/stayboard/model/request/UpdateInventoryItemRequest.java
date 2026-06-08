package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.InventoryUnitType;
import az.aladdin.stayboard.model.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateInventoryItemRequest(
        @NotBlank String name,
        String sku,
        String description,
        boolean active,
        @NotNull InventoryUnitType unitType,
        @NotNull UnitOfMeasure unitOfMeasure,
        @PositiveOrZero BigDecimal reorderLevel
) {
}
