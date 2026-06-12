package az.aladdin.stayboard.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ModifierOptionResponse(
        Long id,
        Long hotelId,
        Long modifierGroupId,
        String optionName,
        BigDecimal priceDelta,
        boolean defaultSelected,
        boolean active,
        int sortOrder,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
