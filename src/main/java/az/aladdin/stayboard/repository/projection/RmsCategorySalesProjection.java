package az.aladdin.stayboard.repository.projection;

import java.math.BigDecimal;

public interface RmsCategorySalesProjection {

    Long getCategoryId();

    String getCategoryName();

    Long getItemLineCount();

    Long getQuantitySold();

    BigDecimal getGrossAmount();

    BigDecimal getNetAmount();

    BigDecimal getTaxAmount();
}
