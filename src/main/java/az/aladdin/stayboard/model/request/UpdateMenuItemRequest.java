package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.InventoryUnitType;
import az.aladdin.stayboard.model.enums.TaxType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateMenuItemRequest(
        @NotBlank String itemName,
        String itemDescription,
        boolean active,
        @NotNull @PositiveOrZero BigDecimal price,
        @PositiveOrZero BigDecimal taxRate,
        TaxType taxType,
        @NotNull InventoryUnitType saleUnitType,
        @NotNull Long menuCategoryId
) {
}
