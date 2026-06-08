package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.InventoryItemEntity;
import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.RecipeEntity;
import az.aladdin.stayboard.model.request.CreateRecipeRequest;
import az.aladdin.stayboard.model.request.UpdateRecipeRequest;
import az.aladdin.stayboard.model.response.RecipeResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeMapper {

    private final HotelTimeService hotelTimeService;

    public RecipeEntity toEntity(
            CreateRecipeRequest request,
            Long hotelId,
            MenuItemEntity menuItem,
            InventoryItemEntity inventoryItem
    ) {
        return RecipeEntity.builder()
                .hotelId(hotelId)
                .menuItem(menuItem)
                .inventoryItem(inventoryItem)
                .quantityPerServing(request.quantityPerServing())
                .build();
    }

    public void updateEntity(
            RecipeEntity entity,
            UpdateRecipeRequest request,
            MenuItemEntity menuItem,
            InventoryItemEntity inventoryItem
    ) {
        entity.setMenuItem(menuItem);
        entity.setInventoryItem(inventoryItem);
        entity.setQuantityPerServing(request.quantityPerServing());
    }

    public RecipeResponse toResponse(RecipeEntity entity) {
        Long hotelId = entity.getHotelId();
        return new RecipeResponse(
                entity.getId(),
                hotelId,
                entity.getMenuItem() != null ? entity.getMenuItem().getId() : null,
                entity.getMenuItem() != null ? entity.getMenuItem().getItemName() : null,
                entity.getInventoryItem() != null ? entity.getInventoryItem().getId() : null,
                entity.getInventoryItem() != null ? entity.getInventoryItem().getName() : null,
                entity.getQuantityPerServing(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy()
        );
    }
}
