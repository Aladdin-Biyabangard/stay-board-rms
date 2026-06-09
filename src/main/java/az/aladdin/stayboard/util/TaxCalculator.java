package az.aladdin.stayboard.util;

import az.aladdin.stayboard.model.enums.TaxType;
import az.aladdin.stayboard.model.pricing.TaxResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class TaxCalculator {

    private static final int SCALE = 2;

    private TaxCalculator() {
    }

    public static TaxResult calculateTax(BigDecimal amount, BigDecimal taxRate, TaxType type) {
        BigDecimal normalizedAmount = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal normalizedRate = taxRate != null ? taxRate : BigDecimal.ZERO;
        TaxType taxType = type != null ? type : TaxType.EXCLUDE;
        BigDecimal rate = normalizeRate(normalizedRate);

        return switch (taxType) {
            case EXCLUDE -> calculateExclude(normalizedAmount, rate);
            case INCLUDE -> calculateInclude(normalizedAmount, rate);
        };
    }

    private static TaxResult calculateExclude(BigDecimal amount, BigDecimal taxRate) {
        BigDecimal taxAmount = amount.multiply(taxRate).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal total = amount.add(taxAmount);
        return new TaxResult(amount, taxAmount, total);
    }

    private static TaxResult calculateInclude(BigDecimal amount, BigDecimal taxRate) {
        BigDecimal divisor = BigDecimal.ONE.add(taxRate);
        BigDecimal taxAmount = amount
                .subtract(amount.divide(divisor, 10, RoundingMode.HALF_UP))
                .setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal net = amount.subtract(taxAmount).setScale(SCALE, RoundingMode.HALF_UP);
        return new TaxResult(net, taxAmount, amount);
    }

    private static BigDecimal normalizeRate(BigDecimal taxRate) {
        if (taxRate.compareTo(BigDecimal.ONE) > 0) {
            return taxRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        }
        return taxRate;
    }
}
