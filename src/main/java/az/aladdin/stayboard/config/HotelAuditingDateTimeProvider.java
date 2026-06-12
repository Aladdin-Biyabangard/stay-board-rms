package az.aladdin.stayboard.config;

import az.aladdin.stayboard.service.common.UtcDateTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.stereotype.Component;

import java.time.temporal.TemporalAccessor;
import java.util.Optional;

@Component("hotelAuditingDateTimeProvider")
@RequiredArgsConstructor
public class HotelAuditingDateTimeProvider implements DateTimeProvider {

    private final UtcDateTimeService utcDateTimeService;

    @Override
    public Optional<TemporalAccessor> getNow() {
        return Optional.of(utcDateTimeService.now());
    }
}
