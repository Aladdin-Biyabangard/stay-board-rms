package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.InventoryUnitType;
import az.aladdin.stayboard.model.enums.UnitOfMeasure;

import java.math.BigDecimal;

public record PatchInventoryItemRequest(
        String name,
        String sku,
        String description,
        Boolean active,
        InventoryUnitType unitType,
        UnitOfMeasure unitOfMeasure,
        BigDecimal reorderLevel
) {
}
