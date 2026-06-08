package az.aladdin.stayboard.model.request.search;

import az.aladdin.stayboard.model.enums.InventoryUnitType;

public record InventoryItemSearchCriteria(
        String name,
        String sku,
        Boolean active,
        InventoryUnitType unitType,
        Boolean lowStock
) {
}
