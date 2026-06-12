package az.aladdin.stayboard.model.response.report;

import az.aladdin.stayboard.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RmsDailyStatisticsResponse(
        String timezone,
        LocalDate businessDate,
        LocalDateTime generatedAt,
        OrderSummary orders,
        RevenueSummary revenue,
        OperationalSnapshot operations
) {

    public record OrderSummary(
            long totalCreated,
            long completed,
            long cancelled,
            long open,
            long roomCharge,
            long dineIn,
            List<StatusCount> byStatus
    ) {
    }

    public record StatusCount(
            OrderStatus status,
            long count
    ) {
    }

    public record RevenueSummary(
            BigDecimal grossAmount,
            BigDecimal netAmount,
            BigDecimal taxAmount,
            long itemLineCount,
            long orderCount,
            BigDecimal averageOrderValue
    ) {
    }

    public record OperationalSnapshot(
            long openOrdersNow,
            long activeTableOccupancies,
            long waitingWaitlist,
            long kitchenTicketsPending
    ) {
    }
}
