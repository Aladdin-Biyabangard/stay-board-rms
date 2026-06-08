package az.aladdin.stayboard.util;

import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.model.enums.OrderItemStatus;

import java.util.EnumSet;
import java.util.Map;

public final class OrderItemStatusTransition {

    private static final Map<OrderItemStatus, EnumSet<OrderItemStatus>> ALLOWED = Map.of(
            OrderItemStatus.ORDERED, EnumSet.of(OrderItemStatus.PREPARING, OrderItemStatus.CANCELLED),
            OrderItemStatus.PREPARING, EnumSet.of(OrderItemStatus.READY, OrderItemStatus.CANCELLED),
            OrderItemStatus.READY, EnumSet.of(OrderItemStatus.SERVED, OrderItemStatus.CANCELLED),
            OrderItemStatus.SERVED, EnumSet.noneOf(OrderItemStatus.class),
            OrderItemStatus.CANCELLED, EnumSet.noneOf(OrderItemStatus.class)
    );

    private OrderItemStatusTransition() {
    }

    public static void validate(OrderItemStatus current, OrderItemStatus target) {
        if (current == target) {
            return;
        }
        EnumSet<OrderItemStatus> allowed = ALLOWED.getOrDefault(current, EnumSet.noneOf(OrderItemStatus.class));
        if (!allowed.contains(target)) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_ORDER_ITEM_STATUS_TRANSITION);
        }
    }

    public static boolean consumesInventory(OrderItemStatus from, OrderItemStatus to) {
        return from == OrderItemStatus.ORDERED && to == OrderItemStatus.PREPARING;
    }

    public static boolean reversesInventory(OrderItemStatus from, OrderItemStatus to) {
        return to == OrderItemStatus.CANCELLED
                && (from == OrderItemStatus.PREPARING || from == OrderItemStatus.READY);
    }
}
