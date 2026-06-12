package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PatchModifierGroupRequest(
        String groupName,
        Boolean required,
        @PositiveOrZero Integer minSelections,
        @PositiveOrZero Integer maxSelections,
        Boolean active,
        Integer sortOrder,
        @PositiveOrZero BigDecimal priceDelta
) {
}
