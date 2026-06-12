package az.aladdin.stayboard.repository.projection;

import java.math.BigDecimal;

public interface RmsRevenueTotalsProjection {

    BigDecimal getGrossAmount();

    BigDecimal getNetAmount();

    BigDecimal getTaxAmount();

    Long getItemLineCount();

    Long getOrderCount();
}
