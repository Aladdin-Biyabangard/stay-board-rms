package az.aladdin.stayboard.audit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AuditMessageSupport {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private AuditMessageSupport() {
    }

    public static Object formatArg(Object arg) {
        if (arg == null) {
            return "";
        }
        if (arg instanceof LocalDateTime dateTime) {
            return dateTime.format(FORMATTER);
        }
        return arg;
    }
}
