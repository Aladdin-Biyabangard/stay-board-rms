package az.aladdin.stayboard.model.request;

import az.aladdin.stayboard.model.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank String orderNumber,
        GuestInformationRequest guestInformation,
        Long tableId,
        String roomNumber,
        @NotNull @PositiveOrZero BigDecimal totalAmount,
        @NotNull OrderStatus orderStatus,
        @Valid List<CreateOrderItemLineRequest> items
) {
}
