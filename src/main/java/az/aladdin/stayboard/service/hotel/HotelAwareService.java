package az.aladdin.stayboard.service.hotel;

import az.aladdin.stayboard.context.HotelContextHolder;

public abstract class HotelAwareService {

    protected Long getCurrentHotelId() {
        return HotelContextHolder.getHotelIdOrThrow();
    }
}
