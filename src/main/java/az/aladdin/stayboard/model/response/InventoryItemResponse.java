package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.InventoryUnitType;
import az.aladdin.stayboard.model.enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryItemResponse(
        Long id,
        Long hotelId,
        String name,
        String sku,
        String description,
        boolean active,
        InventoryUnitType unitType,
        UnitOfMeasure unitOfMeasure,
        BigDecimal currentStock,
        BigDecimal reorderLevel,
        boolean lowStock,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
