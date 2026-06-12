package az.aladdin.stayboard.repository.projection;

import java.math.BigDecimal;

public interface RmsTopMenuItemProjection {

    Long getMenuItemId();

    String getMenuItemName();

    String getCategoryName();

    Long getQuantitySold();

    BigDecimal getGrossAmount();
}
