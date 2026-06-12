package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateModifierGroupRequest(
        @NotBlank String groupName,
        boolean required,
        @PositiveOrZero int minSelections,
        @PositiveOrZero int maxSelections,
        boolean active,
        int sortOrder,
        @PositiveOrZero BigDecimal priceDelta
) {
}
