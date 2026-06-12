package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderReceiptResponse(
        String receiptNumber,
        Long orderId,
        String orderNumber,
        String guestName,
        String tableNumber,
        String roomNumber,
        OrderStatus orderStatus,
        LocalDateTime receiptDate,
        String timezone,
        String currencyCode,
        List<OrderReceiptLineItem> items,
        BigDecimal subtotalNet,
        BigDecimal totalTax,
        BigDecimal totalGross,
        BigDecimal orderTotal
) {
}
