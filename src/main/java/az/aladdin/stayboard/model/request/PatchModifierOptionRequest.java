package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PatchModifierOptionRequest(
        Long modifierGroupId,
        String optionName,
        @PositiveOrZero BigDecimal priceDelta,
        Boolean defaultSelected,
        Boolean active,
        Integer sortOrder
) {
}
