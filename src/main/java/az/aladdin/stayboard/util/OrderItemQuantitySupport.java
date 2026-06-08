package az.aladdin.stayboard.util;

import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.model.enums.InventoryUnitType;

import java.math.BigDecimal;

public final class OrderItemQuantitySupport {

    private OrderItemQuantitySupport() {
    }

    public static void validate(MenuItemEntity menuItem, long quantity, BigDecimal weightQuantity) {
        if (menuItem.getSaleUnitType() == InventoryUnitType.WEIGHT) {
            if (weightQuantity == null || weightQuantity.signum() <= 0) {
                throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_WEIGHT_QUANTITY_REQUIRED);
            }
            return;
        }
        if (quantity <= 0) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_QUANTITY_REQUIRED);
        }
    }

    public static BigDecimal effectiveServingQuantity(OrderItemEntity orderItem) {
        MenuItemEntity menuItem = orderItem.getMenuItem();
        if (menuItem != null && menuItem.getSaleUnitType() == InventoryUnitType.WEIGHT) {
            return orderItem.getWeightQuantity() != null ? orderItem.getWeightQuantity() : BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(orderItem.getQuantity());
    }
}
