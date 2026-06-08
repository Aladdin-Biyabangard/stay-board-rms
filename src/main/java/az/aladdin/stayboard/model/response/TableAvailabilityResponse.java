package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.TableAvailabilityStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TableAvailabilityResponse(
        Long tableId,
        String tableNumber,
        int capacity,
        int maxCapacity,
        List<String> amenities,
        TableAvailabilityStatus status,
        LocalDateTime statusUntil,
        LocalDateTime nextAvailableAt,
        boolean reservable,
        Long currentOccupancyId
) {
}
