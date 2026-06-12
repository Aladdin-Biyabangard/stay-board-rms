package az.aladdin.stayboard.service.print;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.response.OrderReceiptLineItem;
import az.aladdin.stayboard.model.response.OrderReceiptResponse;
import az.aladdin.stayboard.repository.OrderItemRepository;
import az.aladdin.stayboard.repository.OrderRepository;
import az.aladdin.stayboard.security.GuestOrderAccess;
import az.aladdin.stayboard.service.common.UtcDateTimeService;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import az.aladdin.stayboard.util.PrintSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static az.aladdin.stayboard.util.PrintSupport.DEFAULT_CURRENCY_CODE;

@Service
@RequiredArgsConstructor
public class OrderReceiptService extends HotelAwareService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final HotelTimeService hotelTimeService;
    private final UtcDateTimeService utcDateTimeService;

    @Transactional(readOnly = true)
    public OrderReceiptResponse generateReceipt(Long orderId) {
        OrderEntity order = fetchAccessibleOrder(orderId);
        Long hotelId = order.getHotelId();
        List<OrderItemEntity> items = orderItemRepository.findAllByOrder_IdAndHotelId(orderId, hotelId).stream()
                .filter(item -> item.getOrderItemStatus() != OrderItemStatus.CANCELLED)
                .sorted(Comparator.comparing(OrderItemEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        BigDecimal subtotalNet = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalGross = BigDecimal.ZERO;

        List<OrderReceiptLineItem> receiptItems = items.stream()
                .map(item -> {
                    BigDecimal net = safeAmount(item.getNetAmount());
                    BigDecimal tax = safeAmount(item.getTaxAmount());
                    BigDecimal gross = safeAmount(item.getGrossAmount());
                    return new OrderReceiptLineItem(
                            item.getId(),
                            item.getMenuItem() != null ? item.getMenuItem().getItemName() : "—",
                            item.getMenuItem() != null ? item.getMenuItem().getSaleUnitType() : null,
                            PrintSupport.formatQuantityLabel(item),
                            net,
                            tax,
                            gross,
                            item.getOrderItemStatus(),
                            hotelTimeService.utcLocalDateTimeToHotelLocal(item.getCreatedAt(), hotelId)
                    );
                })
                .toList();

        for (OrderItemEntity item : items) {
            subtotalNet = subtotalNet.add(safeAmount(item.getNetAmount()));
            totalTax = totalTax.add(safeAmount(item.getTaxAmount()));
            totalGross = totalGross.add(safeAmount(item.getGrossAmount()));
        }

        String tableNumber = order.getTableEntity() != null ? order.getTableEntity().getTableNumber() : null;
        return new OrderReceiptResponse(
                "REC-" + order.getOrderNumber(),
                order.getId(),
                order.getOrderNumber(),
                PrintSupport.formatGuestName(order.getGuestInformation()),
                tableNumber,
                order.getRoomNumber(),
                order.getOrderStatus(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(utcDateTimeService.now(), hotelId),
                hotelTimeService.resolveHotelTimezone(hotelId),
                DEFAULT_CURRENCY_CODE,
                receiptItems,
                subtotalNet,
                totalTax,
                totalGross,
                order.getTotalAmount() != null ? order.getTotalAmount() : totalGross
        );
    }

    private OrderEntity fetchAccessibleOrder(Long orderId) {
        OrderEntity order = orderRepository.findByIdAndHotelId(orderId, getCurrentHotelId())
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.ORDER));
        GuestOrderAccess.ensureGuestCanAccessOrder(order);
        return order;
    }

    private static BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }
}
