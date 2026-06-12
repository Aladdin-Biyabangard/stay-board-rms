package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateModifierOptionRequest(
        @NotNull Long modifierGroupId,
        @NotBlank String optionName,
        @PositiveOrZero BigDecimal priceDelta,
        boolean defaultSelected,
        boolean active,
        int sortOrder
) {
}
