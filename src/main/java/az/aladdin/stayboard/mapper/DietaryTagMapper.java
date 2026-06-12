package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.DietaryTagEntity;
import az.aladdin.stayboard.model.request.CreateDietaryTagRequest;
import az.aladdin.stayboard.model.request.PatchDietaryTagRequest;
import az.aladdin.stayboard.model.response.DietaryTagResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DietaryTagMapper {

    private final HotelTimeService hotelTimeService;

    public DietaryTagEntity toEntity(CreateDietaryTagRequest request, Long hotelId) {
        return DietaryTagEntity.builder()
                .hotelId(hotelId)
                .tagName(request.tagName())
                .description(request.description())
                .active(request.active())
                .build();
    }

    public void updateEntity(DietaryTagEntity entity, CreateDietaryTagRequest request) {
        entity.setTagName(request.tagName());
        entity.setDescription(request.description());
        entity.setActive(request.active());
    }

    public void patchEntity(DietaryTagEntity entity, PatchDietaryTagRequest request) {
        if (request.tagName() != null) {
            entity.setTagName(request.tagName());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }

    public DietaryTagResponse toResponse(DietaryTagEntity entity) {
        Long hotelId = entity.getHotelId();
        return new DietaryTagResponse(
                entity.getId(),
                hotelId,
                entity.getTagName(),
                entity.getDescription(),
                entity.isActive(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy()
        );
    }
}
