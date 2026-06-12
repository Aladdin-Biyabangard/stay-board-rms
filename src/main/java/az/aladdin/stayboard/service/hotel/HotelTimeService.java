package az.aladdin.stayboard.service.hotel;

import az.aladdin.stayboard.annotation.NoLogging;
import az.aladdin.stayboard.client.StayBoardHotelClient;
import az.aladdin.stayboard.model.response.HotelTimezoneResponse;
import az.aladdin.stayboard.service.common.UtcDateTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelTimeService {

    private static final ZoneId FALLBACK_ZONE = ZoneOffset.UTC;
    private final ConcurrentHashMap<Long, ZoneId> zoneIdCache = new ConcurrentHashMap<>();
    private final StayBoardHotelClient stayBoardHotelClient;
    private final UtcDateTimeService utcDateTimeService;

    public void evictHotelZoneId(Long hotelId) {
        if (hotelId != null) {
            zoneIdCache.remove(hotelId);
        }
    }

    public LocalDate todayAtHotel(Long hotelId) {
        return utcDateTimeService.instant()
                .atZone(resolveHotelZoneId(hotelId))
                .toLocalDate();
    }

    public LocalDateTime nowAtHotel(Long hotelId) {
        return utcDateTimeService.instant()
                .atZone(resolveHotelZoneId(hotelId))
                .toLocalDateTime();
    }

    public LocalDateTime startOfDayUtc(LocalDate hotelDate, Long hotelId) {
        return hotelDate
                .atStartOfDay(resolveHotelZoneId(hotelId))
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    public LocalDateTime nextDayStartUtc(LocalDate hotelDate, Long hotelId) {
        return hotelDate
                .plusDays(1)
                .atStartOfDay(resolveHotelZoneId(hotelId))
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    public LocalDateTime hotelLocalDateTimeToUtc(LocalDateTime hotelLocalDateTime, Long hotelId) {
        if (hotelLocalDateTime == null) {
            return null;
        }
        return hotelLocalDateTime
                .atZone(resolveHotelZoneId(hotelId))
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    @NoLogging
    public LocalDateTime utcLocalDateTimeToHotelLocal(LocalDateTime utcLocalDateTime, Long hotelId) {
        if (utcLocalDateTime == null) {
            return null;
        }
        return utcLocalDateTime
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(resolveHotelZoneId(hotelId))
                .toLocalDateTime();
    }

    public ZoneId resolveHotelZoneId(Long hotelId) {
        return zoneIdCache.computeIfAbsent(hotelId, this::loadHotelZoneId);
    }

    public String resolveHotelTimezone(Long hotelId) {
        return resolveHotelZoneId(hotelId).getId();
    }

    private ZoneId loadHotelZoneId(Long hotelId) {
        String timezone = FALLBACK_ZONE.getId();
        try {
            HotelTimezoneResponse hotel = loadHotelTimezone(hotelId);
            if (hotel != null && hotel.getTimezone() != null && !hotel.getTimezone().isBlank()) {
                timezone = hotel.getTimezone();
            }
        } catch (Exception ex) {
            log.warn("Failed to load timezone for hotel {}. Falling back to UTC.", hotelId, ex);
        }

        try {
            return ZoneId.of(timezone);
        } catch (Exception ex) {
            log.warn("Invalid timezone '{}' for hotel {}. Falling back to UTC.", timezone, hotelId);
            return FALLBACK_ZONE;
        }
    }

    private HotelTimezoneResponse loadHotelTimezone(Long hotelId) {
        HotelTimezoneResponse currentHotel = stayBoardHotelClient.getCurrentHotel();
        if (currentHotel != null
                && currentHotel.getId() != null
                && currentHotel.getId().equals(hotelId)
                && currentHotel.getTimezone() != null
                && !currentHotel.getTimezone().isBlank()) {
            return currentHotel;
        }
        return stayBoardHotelClient.getHotel(hotelId);
    }
}
