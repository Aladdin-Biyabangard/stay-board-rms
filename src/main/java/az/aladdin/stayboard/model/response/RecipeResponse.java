package az.aladdin.stayboard.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecipeResponse(
        Long id,
        Long hotelId,
        Long menuItemId,
        String menuItemName,
        Long inventoryItemId,
        String inventoryItemName,
        BigDecimal quantityPerServing,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
