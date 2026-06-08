package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.InventoryUnitType;
import az.aladdin.stayboard.model.enums.TaxType;

import java.math.BigDecimal;

public record PatchMenuItemRequest(
        String itemName,
        String itemDescription,
        Boolean active,
        BigDecimal price,
        BigDecimal taxRate,
        TaxType taxType,
        InventoryUnitType saleUnitType,
        Long menuCategoryId
) {
}
