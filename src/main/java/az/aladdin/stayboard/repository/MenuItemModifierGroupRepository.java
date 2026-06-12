package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.MenuItemModifierGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemModifierGroupRepository extends JpaRepository<MenuItemModifierGroupEntity, Long> {

    List<MenuItemModifierGroupEntity> findByMenuItem_IdAndHotelIdOrderBySortOrderAsc(Long menuItemId, Long hotelId);

    boolean existsByModifierGroup_IdAndHotelId(Long modifierGroupId, Long hotelId);
}
