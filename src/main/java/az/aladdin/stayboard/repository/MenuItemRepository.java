package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItemEntity, Long>, JpaSpecificationExecutor<MenuItemEntity> {

    Optional<MenuItemEntity> findByIdAndHotelId(Long id, Long hotelId);

    boolean existsByMenuCategory_IdAndHotelId(Long menuCategoryId, Long hotelId);
}
