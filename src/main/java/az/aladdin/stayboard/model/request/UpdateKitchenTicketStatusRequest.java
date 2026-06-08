package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateKitchenTicketStatusRequest(
        @NotNull OrderItemStatus orderItemStatus
) {
}
