package az.aladdin.stayboard.model.response.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RmsTopItemsReportResponse(
        String timezone,
        LocalDate fromDate,
        LocalDate toDate,
        LocalDateTime generatedAt,
        List<ItemRow> items
) {

    public record ItemRow(
            Long menuItemId,
            String menuItemName,
            String categoryName,
            long quantitySold,
            BigDecimal grossAmount
    ) {
    }
}
