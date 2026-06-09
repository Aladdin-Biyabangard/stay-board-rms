package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.SaleUnitType;
import az.aladdin.stayboard.model.enums.TaxType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record MenuItemResponse(
        Long id,
        Long hotelId,
        String itemName,
        String itemDescription,
        boolean active,
        BigDecimal price,
        BigDecimal taxRate,
        TaxType taxType,
        SaleUnitType saleUnitType,
        Long menuCategoryId,
        String menuCategoryName,
        Set<String> photoUrls,
        String mainImageUrl,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
