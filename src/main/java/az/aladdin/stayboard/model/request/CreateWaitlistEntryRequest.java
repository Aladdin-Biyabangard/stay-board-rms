package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateWaitlistEntryRequest(
        @NotNull @Positive Integer partySize,
        ReservationMainInfoRequest reservationMainInfo,
        String notes,
        Long preferredTableId,
        @Positive Integer estimatedWaitMinutes
) {
}
