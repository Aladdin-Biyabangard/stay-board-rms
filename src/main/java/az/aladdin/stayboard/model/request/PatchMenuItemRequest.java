package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.SaleUnitType;
import az.aladdin.stayboard.model.enums.TaxType;

import java.math.BigDecimal;

public record PatchMenuItemRequest(
        String itemName,
        String itemDescription,
        Boolean active,
        BigDecimal price,
        BigDecimal taxRate,
        TaxType taxType,
        SaleUnitType saleUnitType,
        Long menuCategoryId
) {
}
