package az.aladdin.stayboard.repository.projection;

import az.aladdin.stayboard.model.enums.OrderStatus;

public interface RmsOrderStatusCountProjection {

    OrderStatus getOrderStatus();

    Long getOrderCount();
}
