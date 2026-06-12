package az.aladdin.stayboard.util;

import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private static final String HOTEL_ID_TOO_LONG = "Hotel id is too long to generate an order number";

    private final HotelTimeService hotelTimeService;

    public String generate(Long hotelId) {
        LocalDate hotelToday = hotelTimeService.todayAtHotel(hotelId);
        String hotelPart = String.valueOf(hotelId);
        String dayPart = String.format("%02d", hotelToday.getDayOfMonth());

        int randomLength = 9 - (hotelPart.length() + dayPart.length());

        if (randomLength <= 0) {
            throw new IllegalArgumentException(HOTEL_ID_TOO_LONG);
        }

        int min = (int) Math.pow(10, randomLength - 1);
        int max = (int) Math.pow(10, randomLength);

        int randomPart = ThreadLocalRandom.current().nextInt(min, max);
        String randomStr = String.valueOf(randomPart);

        String orderNumber = "O" + hotelPart + "." + dayPart + "." + randomStr;

        log.debug("Generated order number: {} for hotel: {}", orderNumber, hotelId);

        return orderNumber;
    }
}
