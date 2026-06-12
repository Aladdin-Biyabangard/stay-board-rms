package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SeatWaitlistEntryRequest(
        @NotNull Long tableId,
        @Positive Integer durationMinutes
) {
}
