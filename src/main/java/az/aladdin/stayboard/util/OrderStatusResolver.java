package az.aladdin.stayboard.util;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.OrderStatus;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public final class OrderStatusResolver {

    private static final Map<OrderItemStatus, OrderStatus> ITEM_TO_ORDER = Map.of(
            OrderItemStatus.ORDERED, OrderStatus.ORDERED,
            OrderItemStatus.PREPARING, OrderStatus.PREPARING,
            OrderItemStatus.READY, OrderStatus.READY,
            OrderItemStatus.SERVED, OrderStatus.SERVED
    );

    private static final Map<OrderItemStatus, Integer> PIPELINE_RANK = new EnumMap<>(OrderItemStatus.class);

    static {
        PIPELINE_RANK.put(OrderItemStatus.ORDERED, 0);
        PIPELINE_RANK.put(OrderItemStatus.PREPARING, 1);
        PIPELINE_RANK.put(OrderItemStatus.READY, 2);
        PIPELINE_RANK.put(OrderItemStatus.SERVED, 3);
    }

    private OrderStatusResolver() {
    }

    /**
     * Derives the aggregate order status from its item statuses.
     * The order reflects the slowest active (non-cancelled) item in the kitchen pipeline.
     */
    public static OrderStatus derive(Collection<OrderItemStatus> itemStatuses) {
        if (itemStatuses == null || itemStatuses.isEmpty()) {
            return OrderStatus.ORDERED;
        }

        boolean hasActiveItem = false;
        OrderItemStatus slowestActive = null;

        for (OrderItemStatus status : itemStatuses) {
            if (status == null || status == OrderItemStatus.CANCELLED) {
                continue;
            }
            hasActiveItem = true;
            if (slowestActive == null || PIPELINE_RANK.get(status) < PIPELINE_RANK.get(slowestActive)) {
                slowestActive = status;
            }
        }

        if (!hasActiveItem) {
            return OrderStatus.CANCELLED;
        }

        return ITEM_TO_ORDER.get(slowestActive);
    }
}
