package az.aladdin.stayboard.service.order;

import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.service.inventory.InventoryConsumptionService;
import az.aladdin.stayboard.util.OrderItemStatusTransition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderItemStatusChangeService {

    private final InventoryConsumptionService inventoryConsumptionService;

    public void applyStatusChange(OrderItemEntity entity, OrderItemStatus fromStatus, OrderItemStatus toStatus) {
        if (fromStatus == toStatus) {
            return;
        }

        OrderItemStatusTransition.validate(fromStatus, toStatus);

        if (OrderItemStatusTransition.consumesInventory(fromStatus, toStatus)) {
            inventoryConsumptionService.consumeForOrderItem(entity);
        }
        if (OrderItemStatusTransition.reversesInventory(fromStatus, toStatus)) {
            inventoryConsumptionService.reverseForOrderItem(entity.getId());
        }

        entity.setOrderItemStatus(toStatus);
    }

    public void reverseInventoryIfConsumed(OrderItemEntity entity) {
        OrderItemStatus status = entity.getOrderItemStatus();
        if (status == OrderItemStatus.PREPARING || status == OrderItemStatus.READY) {
            inventoryConsumptionService.reverseForOrderItem(entity.getId());
        }
    }
}
