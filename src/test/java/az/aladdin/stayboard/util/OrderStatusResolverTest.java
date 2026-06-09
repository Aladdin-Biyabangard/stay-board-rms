package az.aladdin.stayboard.util;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderStatusResolverTest {

    @Test
    void derive_returnsOrdered_whenAllItemsOrdered() {
        assertEquals(
                OrderStatus.ORDERED,
                OrderStatusResolver.derive(List.of(OrderItemStatus.ORDERED, OrderItemStatus.ORDERED))
        );
    }

    @Test
    void derive_returnsPreparing_whenAnyActiveItemIsPreparing() {
        assertEquals(
                OrderStatus.PREPARING,
                OrderStatusResolver.derive(List.of(OrderItemStatus.READY, OrderItemStatus.PREPARING))
        );
    }

    @Test
    void derive_returnsReady_whenAllActiveItemsReadyOrServed() {
        assertEquals(
                OrderStatus.READY,
                OrderStatusResolver.derive(List.of(OrderItemStatus.READY, OrderItemStatus.SERVED))
        );
    }

    @Test
    void derive_returnsServed_whenAllActiveItemsServed() {
        assertEquals(
                OrderStatus.SERVED,
                OrderStatusResolver.derive(List.of(OrderItemStatus.SERVED, OrderItemStatus.SERVED))
        );
    }

    @Test
    void derive_returnsCancelled_whenAllItemsCancelled() {
        assertEquals(
                OrderStatus.CANCELLED,
                OrderStatusResolver.derive(List.of(OrderItemStatus.CANCELLED, OrderItemStatus.CANCELLED))
        );
    }

    @Test
    void derive_ignoresCancelledItems() {
        assertEquals(
                OrderStatus.READY,
                OrderStatusResolver.derive(List.of(OrderItemStatus.CANCELLED, OrderItemStatus.READY))
        );
    }
}
