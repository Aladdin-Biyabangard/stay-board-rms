package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.MenuCategoryEntity;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;

public interface MenuCategoryRepository extends HotelAwareSpecificationRepository<MenuCategoryEntity, Long> {

    boolean existsByIdAndHotelId(Long id, Long hotelId);
}
