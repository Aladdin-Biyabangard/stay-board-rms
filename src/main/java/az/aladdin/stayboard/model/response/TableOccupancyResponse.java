package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.OccupancySourceType;

import java.time.LocalDateTime;

public record TableOccupancyResponse(
        Long id,
        Long hotelId,
        Long tableId,
        String tableNumber,
        OccupancySourceType sourceType,
        ReservationMainInfoResponse reservationMainInfo,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer partySize,
        LocalDateTime createdAt,
        String createdBy,
        boolean ownedByCurrentGuest
) {
}
