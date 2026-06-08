package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateRecipeRequest(
        @NotNull Long menuItemId,
        @NotNull Long inventoryItemId,
        @NotNull @Positive BigDecimal quantityPerServing
) {
}
