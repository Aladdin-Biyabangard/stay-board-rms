package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.MenuCategoryEntity;
import az.aladdin.stayboard.model.request.CreateMenuCategoryRequest;
import az.aladdin.stayboard.model.request.PatchMenuCategoryRequest;
import az.aladdin.stayboard.model.request.UpdateMenuCategoryRequest;
import az.aladdin.stayboard.model.response.MenuCategoryResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuCategoryMapper {

    private final HotelTimeService hotelTimeService;

    public MenuCategoryEntity toEntity(CreateMenuCategoryRequest request, Long hotelId) {
        return MenuCategoryEntity.builder()
                .hotelId(hotelId)
                .categoryName(request.categoryName())
                .description(request.description())
                .build();
    }

    public void updateEntity(MenuCategoryEntity entity, CreateMenuCategoryRequest request) {
        entity.setCategoryName(request.categoryName());
        entity.setDescription(request.description());
    }

    public void patchEntity(MenuCategoryEntity entity, PatchMenuCategoryRequest request) {
        if (request.categoryName() != null) {
            entity.setCategoryName(request.categoryName());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
    }

    public MenuCategoryResponse toResponse(MenuCategoryEntity entity) {
        Long hotelId = entity.getHotelId();
        return new MenuCategoryResponse(
                entity.getId(),
                hotelId,
                entity.getCategoryName(),
                entity.getDescription(),
                entity.getPhotoUrls(),
                entity.getMainImageUrl(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy()
        );
    }
}
