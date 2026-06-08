package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateOrderRequest(
        @NotBlank String orderNumber,
        GuestInformationRequest guestInformation,
        Long tableId,
        String roomNumber,
        @NotNull @PositiveOrZero BigDecimal totalAmount,
        @NotNull OrderStatus orderStatus
) {
}
