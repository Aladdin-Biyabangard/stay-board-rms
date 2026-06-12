package az.aladdin.stayboard.repository.projection;

import java.math.BigDecimal;

public interface RmsOrderChannelCountProjection {

    Boolean getRoomCharge();

    Long getOrderCount();

    BigDecimal getGrossAmount();
}
