package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.WaitlistStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateWaitlistStatusRequest(
        @NotNull WaitlistStatus status
) {
}
