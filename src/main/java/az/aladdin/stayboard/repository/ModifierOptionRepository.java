package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.ModifierOptionEntity;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;

import java.util.Collection;
import java.util.List;

public interface ModifierOptionRepository extends HotelAwareSpecificationRepository<ModifierOptionEntity, Long> {

    List<ModifierOptionEntity> findByIdInAndHotelId(Collection<Long> ids, Long hotelId);

    List<ModifierOptionEntity> findByModifierGroup_IdAndHotelIdOrderBySortOrderAsc(Long modifierGroupId, Long hotelId);

    boolean existsByModifierGroup_IdAndHotelId(Long modifierGroupId, Long hotelId);
}
