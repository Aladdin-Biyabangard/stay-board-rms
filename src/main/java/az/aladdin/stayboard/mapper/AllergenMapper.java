package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.AllergenEntity;
import az.aladdin.stayboard.model.request.CreateAllergenRequest;
import az.aladdin.stayboard.model.request.PatchAllergenRequest;
import az.aladdin.stayboard.model.response.AllergenResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AllergenMapper {

    private final HotelTimeService hotelTimeService;

    public AllergenEntity toEntity(CreateAllergenRequest request, Long hotelId) {
        return AllergenEntity.builder()
                .hotelId(hotelId)
                .allergenName(request.allergenName())
                .description(request.description())
                .active(request.active())
                .build();
    }

    public void updateEntity(AllergenEntity entity, CreateAllergenRequest request) {
        entity.setAllergenName(request.allergenName());
        entity.setDescription(request.description());
        entity.setActive(request.active());
    }

    public void patchEntity(AllergenEntity entity, PatchAllergenRequest request) {
        if (request.allergenName() != null) {
            entity.setAllergenName(request.allergenName());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }

    public AllergenResponse toResponse(AllergenEntity entity) {
        Long hotelId = entity.getHotelId();
        return new AllergenResponse(
                entity.getId(),
                hotelId,
                entity.getAllergenName(),
                entity.getDescription(),
                entity.isActive(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy()
        );
    }
}
