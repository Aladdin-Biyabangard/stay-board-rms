package az.aladdin.stayboard.service.hotel;

import az.aladdin.stayboard.context.HotelContextHolder;
import az.aladdin.stayboard.exception.ApiExceptions;

import java.util.Optional;

public abstract class HotelAwareService {

    protected Long getCurrentHotelId() {
        return HotelContextHolder.getHotelIdOrThrow();
    }

    protected <T> T requireEntity(Optional<T> entity, String entityKey) {
        return entity.orElseThrow(() -> ApiExceptions.notFound(entityKey));
    }
}
