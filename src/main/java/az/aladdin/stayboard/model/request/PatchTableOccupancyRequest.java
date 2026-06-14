package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.OccupancySourceType;

import java.time.LocalDateTime;

public record PatchTableOccupancyRequest(
        Long tableId,
        OccupancySourceType sourceType,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer partySize,
        ReservationMainInfoRequest reservationMainInfo
) {
}
