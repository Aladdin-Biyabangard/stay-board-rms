package az.aladdin.stayboard.service;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.OrderStatus;
import az.aladdin.stayboard.repository.OrderItemRepository;
import az.aladdin.stayboard.repository.OrderRepository;
import az.aladdin.stayboard.util.OrderStatusResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderStatusSyncService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemFolioSyncService orderItemFolioSyncService;

    public void syncFromOrderItems(OrderEntity order) {
        if (order == null || order.getId() == null) {
            return;
        }

        OrderStatus currentStatus = order.getOrderStatus();
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            return;
        }

        List<OrderItemEntity> items = orderItemRepository.findAllByOrder_IdAndHotelId(order.getId(), order.getHotelId());
        if (items.isEmpty()) {
            return;
        }

        List<OrderItemStatus> itemStatuses = items.stream()
                .map(OrderItemEntity::getOrderItemStatus)
                .toList();
        OrderStatus derivedStatus = OrderStatusResolver.derive(itemStatuses);
        if (derivedStatus == currentStatus) {
            return;
        }

        order.setOrderStatus(derivedStatus);
        orderRepository.save(order);

        if (derivedStatus == OrderStatus.CANCELLED) {
            orderItemFolioSyncService.voidChargesForOrder(order);
        }
    }
}
