package az.aladdin.stayboard.service;

import az.aladdin.stayboard.annotation.NoLogging;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
@NoLogging
@Service
public class UtcDateTimeService {

    private static final Clock UTC_CLOCK = Clock.systemUTC();

    public LocalDateTime now() {
        return LocalDateTime.now(UTC_CLOCK);
    }

    public LocalDate today() {
        return LocalDate.now(UTC_CLOCK);
    }

    public Instant instant() {
        return Instant.now(UTC_CLOCK);
    }

    public LocalDateTime toUtcLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}

