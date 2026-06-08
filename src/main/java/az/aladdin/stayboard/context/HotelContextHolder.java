package az.aladdin.stayboard.context;

import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.MessageKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HotelContextHolder {

    private static final ThreadLocal<Long> hotelContext = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isDirector = new ThreadLocal<>();

    private HotelContextHolder() {
    }

    public static void setHotelId(Long hotelId) {
        hotelContext.set(hotelId);
        log.debug("Hotel context set to: {}", hotelId);
    }

    public static Long getHotelId() {
        return hotelContext.get();
    }

    public static void setIsDirector(Boolean director) {
        isDirector.set(director);
    }

    public static boolean isDirector() {
        return Boolean.TRUE.equals(isDirector.get());
    }

    public static void clear() {
        hotelContext.remove();
        isDirector.remove();
        log.debug("Hotel context cleared");
    }

    public static Long getHotelIdOrThrow() {
        Long hotelId = getHotelId();
        if (hotelId == null) {
            throw ApiExceptions.unauthorized(MessageKey.HOTEL_CONTEXT_NOT_SET);
        }
        return hotelId;
    }
}
