package az.aladdin.stayboard.service.print;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.response.KitchenTicketPrintLine;
import az.aladdin.stayboard.model.response.KitchenTicketPrintResponse;
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

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KitchenTicketPrintService extends HotelAwareService {

    private static final Set<OrderItemStatus> KITCHEN_ACTIVE_STATUSES = EnumSet.of(
            OrderItemStatus.ORDERED,
            OrderItemStatus.PREPARING,
            OrderItemStatus.READY
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final HotelTimeService hotelTimeService;
    private final UtcDateTimeService utcDateTimeService;

    @Transactional(readOnly = true)
    public KitchenTicketPrintResponse generateForOrderItem(Long orderItemId) {
        OrderItemEntity item = fetchOrderItem(orderItemId);
        OrderEntity order = item.getOrder();
        return buildResponse(order, List.of(item));
    }

    @Transactional(readOnly = true)
    public KitchenTicketPrintResponse generateForOrder(Long orderId) {
        OrderEntity order = fetchAccessibleOrder(orderId);
        List<OrderItemEntity> items = orderItemRepository.findAllByOrder_IdAndHotelId(orderId, order.getHotelId()).stream()
                .filter(item -> KITCHEN_ACTIVE_STATUSES.contains(item.getOrderItemStatus()))
                .sorted(Comparator.comparing(OrderItemEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        return buildResponse(order, items);
    }

    private KitchenTicketPrintResponse buildResponse(OrderEntity order, List<OrderItemEntity> items) {
        Long hotelId = order.getHotelId();
        String tableNumber = order.getTableEntity() != null ? order.getTableEntity().getTableNumber() : null;
        String roomNumber = order.getRoomNumber();
        List<KitchenTicketPrintLine> lines = items.stream()
                .map(item -> new KitchenTicketPrintLine(
                        item.getId(),
                        item.getMenuItem() != null ? item.getMenuItem().getItemName() : "—",
                        item.getMenuItem() != null ? item.getMenuItem().getSaleUnitType() : null,
                        PrintSupport.formatQuantityLabel(item),
                        item.getOrderItemStatus(),
                        hotelTimeService.utcLocalDateTimeToHotelLocal(item.getCreatedAt(), hotelId)
                ))
                .toList();

        return new KitchenTicketPrintResponse(
                "KT-" + order.getOrderNumber(),
                order.getId(),
                order.getOrderNumber(),
                tableNumber,
                roomNumber,
                PrintSupport.resolveServiceLocation(tableNumber, roomNumber),
                hotelTimeService.utcLocalDateTimeToHotelLocal(utcDateTimeService.now(), hotelId),
                hotelTimeService.resolveHotelTimezone(hotelId),
                lines
        );
    }

    private OrderItemEntity fetchOrderItem(Long orderItemId) {
        OrderItemEntity item = orderItemRepository.findByIdAndHotelId(orderItemId, getCurrentHotelId())
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.ORDER_ITEM));
        if (item.getOrder() == null) {
            throw ApiExceptions.notFound(EntityKey.ORDER);
        }
        GuestOrderAccess.ensureGuestCanAccessOrder(item.getOrder());
        return item;
    }

    private OrderEntity fetchAccessibleOrder(Long orderId) {
        OrderEntity order = orderRepository.findByIdAndHotelId(orderId, getCurrentHotelId())
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.ORDER));
        GuestOrderAccess.ensureGuestCanAccessOrder(order);
        return order;
    }
}
