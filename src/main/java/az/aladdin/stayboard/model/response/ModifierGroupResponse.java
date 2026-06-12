package az.aladdin.stayboard.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ModifierGroupResponse(
        Long id,
        Long hotelId,
        String groupName,
        boolean required,
        int minSelections,
        int maxSelections,
        boolean active,
        int sortOrder,
        BigDecimal priceDelta,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
