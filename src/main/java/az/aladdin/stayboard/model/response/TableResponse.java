package az.aladdin.stayboard.model.response;

import java.time.LocalDateTime;
import java.util.List;

public record TableResponse(
        Long id,
        Long hotelId,
        String tableNumber,
        int capacity,
        int maxCapacity,
        boolean mergeable,
        String mergeGroupId,
        Long primaryTableId,
        List<String> amenities,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
