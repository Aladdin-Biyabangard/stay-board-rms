package az.aladdin.stayboard.model.request.search;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.TaxType;

public record OrderItemSearchCriteria(
        Long orderId,
        Long menuItemId,
        OrderItemStatus orderItemStatus,
        TaxType taxType
) {
}
