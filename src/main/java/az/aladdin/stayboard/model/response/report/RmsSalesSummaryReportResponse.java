package az.aladdin.stayboard.model.response.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RmsSalesSummaryReportResponse(
        String timezone,
        LocalDate fromDate,
        LocalDate toDate,
        LocalDateTime generatedAt,
        Summary summary,
        List<DailyRow> dailyTrend,
        List<ChannelRow> byChannel
) {

    public record Summary(
            BigDecimal grossAmount,
            BigDecimal netAmount,
            BigDecimal taxAmount,
            long orderCount,
            long itemLineCount,
            BigDecimal averageOrderValue
    ) {
    }

    public record DailyRow(
            LocalDate date,
            BigDecimal grossAmount,
            BigDecimal netAmount,
            BigDecimal taxAmount,
            long orderCount
    ) {
    }

    public record ChannelRow(
            String channel,
            long orderCount,
            BigDecimal grossAmount
    ) {
    }
}
