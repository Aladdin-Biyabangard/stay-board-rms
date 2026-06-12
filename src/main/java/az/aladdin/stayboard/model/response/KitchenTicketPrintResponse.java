package az.aladdin.stayboard.model.response;

import java.time.LocalDateTime;
import java.util.List;

public record KitchenTicketPrintResponse(
        String ticketNumber,
        Long orderId,
        String orderNumber,
        String tableNumber,
        String roomNumber,
        String serviceLocation,
        LocalDateTime ticketDate,
        String timezone,
        List<KitchenTicketPrintLine> items
) {
}
