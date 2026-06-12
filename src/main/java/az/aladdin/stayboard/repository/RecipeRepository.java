package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.RecipeEntity;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;

import java.util.List;

public interface RecipeRepository extends HotelAwareSpecificationRepository<RecipeEntity, Long> {

    List<RecipeEntity> findByMenuItemIdAndHotelId(Long menuItemId, Long hotelId);

    boolean existsByMenuItem_IdAndInventoryItem_IdAndHotelId(Long menuItemId, Long inventoryItemId, Long hotelId);

    boolean existsByInventoryItem_IdAndHotelId(Long inventoryItemId, Long hotelId);
}
