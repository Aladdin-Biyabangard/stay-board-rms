package az.aladdin.stayboard.service.report.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class RmsReportMoneySupport {

    private RmsReportMoneySupport() {
    }

    public static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    public static long safeLong(Long value) {
        return value != null ? value : 0L;
    }

    public static BigDecimal scale(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal average(BigDecimal total, long count) {
        if (count <= 0) {
            return scale(BigDecimal.ZERO);
        }
        return scale(total.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP));
    }
}
