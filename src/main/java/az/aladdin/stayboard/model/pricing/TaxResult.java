package az.aladdin.stayboard.model.pricing;

import java.math.BigDecimal;

public record TaxResult(
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal grossAmount
) {
}
