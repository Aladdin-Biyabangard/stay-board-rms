package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Long>, JpaSpecificationExecutor<RecipeEntity> {

    Optional<RecipeEntity> findByIdAndHotelId(Long id, Long hotelId);

    List<RecipeEntity> findByMenuItemIdAndHotelId(Long menuItemId, Long hotelId);

    boolean existsByMenuItem_IdAndInventoryItem_IdAndHotelId(Long menuItemId, Long inventoryItemId, Long hotelId);

    boolean existsByInventoryItem_IdAndHotelId(Long inventoryItemId, Long hotelId);
}
