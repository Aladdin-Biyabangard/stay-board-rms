package az.aladdin.stayboard.service.report;

import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.OrderStatus;
import az.aladdin.stayboard.model.enums.WaitlistStatus;
import az.aladdin.stayboard.model.response.report.RmsDailyStatisticsResponse;
import az.aladdin.stayboard.model.response.report.RmsSalesByCategoryReportResponse;
import az.aladdin.stayboard.model.response.report.RmsSalesSummaryReportResponse;
import az.aladdin.stayboard.model.response.report.RmsTopItemsReportResponse;
import az.aladdin.stayboard.repository.RmsReportRepository;
import az.aladdin.stayboard.repository.WaitlistEntryRepository;
import az.aladdin.stayboard.repository.projection.RmsCategorySalesProjection;
import az.aladdin.stayboard.repository.projection.RmsOrderChannelCountProjection;
import az.aladdin.stayboard.repository.projection.RmsOrderStatusCountProjection;
import az.aladdin.stayboard.repository.projection.RmsRevenueTotalsProjection;
import az.aladdin.stayboard.repository.projection.RmsTopMenuItemProjection;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import az.aladdin.stayboard.service.report.support.RmsReportDateRangeResolver;
import az.aladdin.stayboard.service.report.support.RmsReportDateRangeResolver.ResolvedBusinessDateRange;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static az.aladdin.stayboard.service.report.support.RmsReportMoneySupport.average;
import static az.aladdin.stayboard.service.report.support.RmsReportMoneySupport.safe;
import static az.aladdin.stayboard.service.report.support.RmsReportMoneySupport.safeLong;
import static az.aladdin.stayboard.service.report.support.RmsReportMoneySupport.scale;

@Service
@RequiredArgsConstructor
public class RmsReportService extends HotelAwareService {

    private static final List<OrderStatus> OPEN_ORDER_STATUSES = List.of(
            OrderStatus.ORDERED,
            OrderStatus.PREPARING,
            OrderStatus.READY,
            OrderStatus.SERVED
    );

    private static final List<OrderItemStatus> KITCHEN_PENDING_STATUSES = List.of(
            OrderItemStatus.ORDERED,
            OrderItemStatus.PREPARING
    );

    private static final List<WaitlistStatus> ACTIVE_WAITLIST_STATUSES = List.of(
            WaitlistStatus.WAITING,
            WaitlistStatus.NOTIFIED
    );

    private final RmsReportRepository rmsReportRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final RmsReportDateRangeResolver dateRangeResolver;
    private final HotelTimeService hotelTimeService;

    @Transactional(readOnly = true)
    public RmsDailyStatisticsResponse getDailyStatistics(LocalDate businessDate) {
        Long hotelId = getCurrentHotelId();
        ResolvedBusinessDateRange range = dateRangeResolver.resolveSingleDay(hotelId, businessDate);

        Map<OrderStatus, Long> statusCounts = toStatusCountMap(
                rmsReportRepository.countOrdersByStatus(hotelId, range.utcStart(), range.utcEndExclusive()));

        long totalCreated = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        long completed = statusCounts.getOrDefault(OrderStatus.COMPLETED, 0L);
        long cancelled = statusCounts.getOrDefault(OrderStatus.CANCELLED, 0L);
        long open = OPEN_ORDER_STATUSES.stream()
                .mapToLong(status -> statusCounts.getOrDefault(status, 0L))
                .sum();

        long roomCharge = 0L;
        long dineIn = 0L;
        for (RmsOrderChannelCountProjection channelRow : rmsReportRepository.countOrdersByChannel(
                hotelId, range.utcStart(), range.utcEndExclusive())) {
            if (Boolean.TRUE.equals(channelRow.getRoomCharge())) {
                roomCharge = safeLong(channelRow.getOrderCount());
            } else {
                dineIn = safeLong(channelRow.getOrderCount());
            }
        }

        RmsRevenueTotalsProjection revenueTotals = rmsReportRepository.sumRevenueTotals(
                hotelId, range.utcStart(), range.utcEndExclusive());
        long revenueOrderCount = revenueTotals != null ? safeLong(revenueTotals.getOrderCount()) : 0L;
        BigDecimal grossAmount = revenueTotals != null ? scale(revenueTotals.getGrossAmount()) : scale(BigDecimal.ZERO);

        LocalDateTime nowAtHotel = hotelTimeService.nowAtHotel(hotelId);

        return new RmsDailyStatisticsResponse(
                range.timezone(),
                range.fromDate(),
                nowAtHotel,
                new RmsDailyStatisticsResponse.OrderSummary(
                        totalCreated,
                        completed,
                        cancelled,
                        open,
                        roomCharge,
                        dineIn,
                        toStatusCountRows(statusCounts)
                ),
                new RmsDailyStatisticsResponse.RevenueSummary(
                        grossAmount,
                        revenueTotals != null ? scale(revenueTotals.getNetAmount()) : scale(BigDecimal.ZERO),
                        revenueTotals != null ? scale(revenueTotals.getTaxAmount()) : scale(BigDecimal.ZERO),
                        revenueTotals != null ? safeLong(revenueTotals.getItemLineCount()) : 0L,
                        revenueOrderCount,
                        average(grossAmount, revenueOrderCount)
                ),
                new RmsDailyStatisticsResponse.OperationalSnapshot(
                        rmsReportRepository.countOrdersByStatuses(hotelId, OPEN_ORDER_STATUSES),
                        rmsReportRepository.countActiveTableOccupancies(hotelId, nowAtHotel),
                        waitlistEntryRepository.countByHotelIdAndStatusIn(hotelId, ACTIVE_WAITLIST_STATUSES),
                        rmsReportRepository.countKitchenTicketsByStatuses(hotelId, KITCHEN_PENDING_STATUSES)
                )
        );
    }

    @Transactional(readOnly = true)
    public RmsSalesSummaryReportResponse getSalesSummary(LocalDate fromDate, LocalDate toDate) {
        Long hotelId = getCurrentHotelId();
        ResolvedBusinessDateRange range = dateRangeResolver.resolveRange(hotelId, fromDate, toDate);

        RmsRevenueTotalsProjection totals = rmsReportRepository.sumRevenueTotals(
                hotelId, range.utcStart(), range.utcEndExclusive());
        long orderCount = totals != null ? safeLong(totals.getOrderCount()) : 0L;
        BigDecimal grossAmount = totals != null ? scale(totals.getGrossAmount()) : scale(BigDecimal.ZERO);

        List<RmsSalesSummaryReportResponse.DailyRow> dailyTrend = new ArrayList<>();
        for (LocalDate day = range.fromDate(); !day.isAfter(range.toDate()); day = day.plusDays(1)) {
            ResolvedBusinessDateRange dayRange = dateRangeResolver.resolveSingleDay(hotelId, day);
            RmsRevenueTotalsProjection dayTotals = rmsReportRepository.sumRevenueTotals(
                    hotelId, dayRange.utcStart(), dayRange.utcEndExclusive());
            dailyTrend.add(new RmsSalesSummaryReportResponse.DailyRow(
                    day,
                    dayTotals != null ? scale(dayTotals.getGrossAmount()) : scale(BigDecimal.ZERO),
                    dayTotals != null ? scale(dayTotals.getNetAmount()) : scale(BigDecimal.ZERO),
                    dayTotals != null ? scale(dayTotals.getTaxAmount()) : scale(BigDecimal.ZERO),
                    dayTotals != null ? safeLong(dayTotals.getOrderCount()) : 0L
            ));
        }

        List<RmsSalesSummaryReportResponse.ChannelRow> byChannel = rmsReportRepository.countOrdersByChannel(
                        hotelId, range.utcStart(), range.utcEndExclusive())
                .stream()
                .map(row -> new RmsSalesSummaryReportResponse.ChannelRow(
                        Boolean.TRUE.equals(row.getRoomCharge()) ? "ROOM_CHARGE" : "DINE_IN",
                        safeLong(row.getOrderCount()),
                        scale(row.getGrossAmount())
                ))
                .toList();

        return new RmsSalesSummaryReportResponse(
                range.timezone(),
                range.fromDate(),
                range.toDate(),
                hotelTimeService.nowAtHotel(hotelId),
                new RmsSalesSummaryReportResponse.Summary(
                        grossAmount,
                        totals != null ? scale(totals.getNetAmount()) : scale(BigDecimal.ZERO),
                        totals != null ? scale(totals.getTaxAmount()) : scale(BigDecimal.ZERO),
                        orderCount,
                        totals != null ? safeLong(totals.getItemLineCount()) : 0L,
                        average(grossAmount, orderCount)
                ),
                dailyTrend,
                byChannel
        );
    }

    @Transactional(readOnly = true)
    public RmsTopItemsReportResponse getTopItems(LocalDate fromDate, LocalDate toDate, int limit) {
        Long hotelId = getCurrentHotelId();
        ResolvedBusinessDateRange range = dateRangeResolver.resolveRange(hotelId, fromDate, toDate);
        int effectiveLimit = Math.max(1, Math.min(limit, 50));

        List<RmsTopItemsReportResponse.ItemRow> items = rmsReportRepository.findTopMenuItems(
                        hotelId,
                        range.utcStart(),
                        range.utcEndExclusive(),
                        PageRequest.of(0, effectiveLimit))
                .stream()
                .map(this::toTopItemRow)
                .toList();

        return new RmsTopItemsReportResponse(
                range.timezone(),
                range.fromDate(),
                range.toDate(),
                hotelTimeService.nowAtHotel(hotelId),
                items
        );
    }

    @Transactional(readOnly = true)
    public RmsSalesByCategoryReportResponse getSalesByCategory(LocalDate fromDate, LocalDate toDate) {
        Long hotelId = getCurrentHotelId();
        ResolvedBusinessDateRange range = dateRangeResolver.resolveRange(hotelId, fromDate, toDate);

        List<RmsSalesByCategoryReportResponse.CategoryRow> categories = rmsReportRepository.sumSalesByCategory(
                        hotelId, range.utcStart(), range.utcEndExclusive())
                .stream()
                .map(this::toCategoryRow)
                .toList();

        return new RmsSalesByCategoryReportResponse(
                range.timezone(),
                range.fromDate(),
                range.toDate(),
                hotelTimeService.nowAtHotel(hotelId),
                categories
        );
    }

    private RmsTopItemsReportResponse.ItemRow toTopItemRow(RmsTopMenuItemProjection projection) {
        return new RmsTopItemsReportResponse.ItemRow(
                projection.getMenuItemId(),
                projection.getMenuItemName(),
                projection.getCategoryName(),
                safeLong(projection.getQuantitySold()),
                scale(projection.getGrossAmount())
        );
    }

    private RmsSalesByCategoryReportResponse.CategoryRow toCategoryRow(RmsCategorySalesProjection projection) {
        return new RmsSalesByCategoryReportResponse.CategoryRow(
                projection.getCategoryId(),
                projection.getCategoryName(),
                safeLong(projection.getItemLineCount()),
                safeLong(projection.getQuantitySold()),
                scale(projection.getGrossAmount()),
                scale(projection.getNetAmount()),
                scale(projection.getTaxAmount())
        );
    }

    private Map<OrderStatus, Long> toStatusCountMap(List<RmsOrderStatusCountProjection> rows) {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (RmsOrderStatusCountProjection row : rows) {
            if (row.getOrderStatus() != null) {
                counts.put(row.getOrderStatus(), safeLong(row.getOrderCount()));
            }
        }
        return counts;
    }

    private List<RmsDailyStatisticsResponse.StatusCount> toStatusCountRows(Map<OrderStatus, Long> statusCounts) {
        return statusCounts.entrySet().stream()
                .map(entry -> new RmsDailyStatisticsResponse.StatusCount(entry.getKey(), entry.getValue()))
                .sorted((left, right) -> left.status().name().compareTo(right.status().name()))
                .toList();
    }
}
