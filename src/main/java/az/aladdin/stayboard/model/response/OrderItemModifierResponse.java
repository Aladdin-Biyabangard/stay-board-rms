package az.aladdin.stayboard.model.response;

import java.math.BigDecimal;

public record OrderItemModifierResponse(
        Long id,
        Long modifierGroupId,
        String modifierName,
        BigDecimal priceDelta
) {
}
