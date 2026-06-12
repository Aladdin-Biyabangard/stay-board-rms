package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;

public interface MenuItemRepository extends HotelAwareSpecificationRepository<MenuItemEntity, Long> {

    boolean existsByMenuCategory_IdAndHotelId(Long menuCategoryId, Long hotelId);
}
