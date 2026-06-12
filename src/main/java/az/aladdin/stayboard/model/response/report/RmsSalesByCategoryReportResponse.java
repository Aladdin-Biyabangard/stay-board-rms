package az.aladdin.stayboard.model.response.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RmsSalesByCategoryReportResponse(
        String timezone,
        LocalDate fromDate,
        LocalDate toDate,
        LocalDateTime generatedAt,
        List<CategoryRow> categories
) {

    public record CategoryRow(
            Long categoryId,
            String categoryName,
            long itemLineCount,
            long quantitySold,
            BigDecimal grossAmount,
            BigDecimal netAmount,
            BigDecimal taxAmount
    ) {
    }
}
