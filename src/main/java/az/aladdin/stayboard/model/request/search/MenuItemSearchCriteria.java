package az.aladdin.stayboard.model.request.search;

import az.aladdin.stayboard.model.enums.TaxType;

import java.math.BigDecimal;

public record MenuItemSearchCriteria(
        String itemName,
        Boolean active,
        Long menuCategoryId,
        TaxType taxType,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
