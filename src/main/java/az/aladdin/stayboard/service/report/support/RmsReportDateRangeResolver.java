package az.aladdin.stayboard.service.report.support;

import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RmsReportDateRangeResolver {

    private final HotelTimeService hotelTimeService;

    public ResolvedBusinessDateRange resolveRange(Long hotelId, LocalDate fromDate, LocalDate toDate) {
        LocalDate hotelToday = hotelTimeService.todayAtHotel(hotelId);
        LocalDate effectiveTo = toDate != null ? toDate : hotelToday;
        LocalDate effectiveFrom = fromDate != null ? fromDate : effectiveTo;

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw ApiExceptions.badRequest(MessageKey.VALIDATION);
        }

        LocalDateTime utcStart = hotelTimeService.startOfDayUtc(effectiveFrom, hotelId);
        LocalDateTime utcEndExclusive = hotelTimeService.nextDayStartUtc(effectiveTo, hotelId);

        return new ResolvedBusinessDateRange(
                effectiveFrom,
                effectiveTo,
                hotelTimeService.resolveHotelTimezone(hotelId),
                utcStart,
                utcEndExclusive
        );
    }

    public ResolvedBusinessDateRange resolveSingleDay(Long hotelId, LocalDate businessDate) {
        return resolveRange(hotelId, businessDate, businessDate);
    }

    public record ResolvedBusinessDateRange(
            LocalDate fromDate,
            LocalDate toDate,
            String timezone,
            LocalDateTime utcStart,
            LocalDateTime utcEndExclusive
    ) {
    }
}
