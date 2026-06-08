package az.aladdin.stayboard.model.request.search;

import az.aladdin.stayboard.model.enums.OccupancySourceType;

import java.time.LocalDateTime;

public record TableOccupancySearchCriteria(
        Long tableId,
        OccupancySourceType sourceType,
        LocalDateTime from,
        LocalDateTime to,
        Boolean mineOnly
) {
}
