package az.aladdin.stayboard.util;

import az.aladdin.stayboard.model.enums.TaxType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxCalculatorTest {

    @Test
    void calculateExcludeTax() {
        var result = TaxCalculator.calculateTax(new BigDecimal("20.00"), new BigDecimal("18"), TaxType.EXCLUDE);

        assertEquals(new BigDecimal("20.00"), result.netAmount());
        assertEquals(new BigDecimal("3.60"), result.taxAmount());
        assertEquals(new BigDecimal("23.60"), result.grossAmount());
    }

    @Test
    void calculateIncludeTax() {
        var result = TaxCalculator.calculateTax(new BigDecimal("11.80"), new BigDecimal("18"), TaxType.INCLUDE);

        assertEquals(new BigDecimal("10.00"), result.netAmount());
        assertEquals(new BigDecimal("1.80"), result.taxAmount());
        assertEquals(new BigDecimal("11.80"), result.grossAmount());
    }

    @Test
    void calculateIncludeTaxForLineTotal() {
        var result = TaxCalculator.calculateTax(new BigDecimal("23.60"), new BigDecimal("18"), TaxType.INCLUDE);

        assertEquals(new BigDecimal("20.00"), result.netAmount());
        assertEquals(new BigDecimal("3.60"), result.taxAmount());
        assertEquals(new BigDecimal("23.60"), result.grossAmount());
    }
}
