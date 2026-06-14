package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.OccupancySourceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record UpdateTableOccupancyRequest(
        @NotNull Long tableId,
        @NotNull OccupancySourceType sourceType,
        @NotNull LocalDateTime startDateTime,
        @NotNull LocalDateTime endDateTime,
        @Positive Integer partySize,
        ReservationMainInfoRequest reservationMainInfo
) {
}
