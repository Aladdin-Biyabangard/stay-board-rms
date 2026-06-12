package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.ModifierGroupEntity;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;

import java.util.Collection;
import java.util.List;

public interface ModifierGroupRepository extends HotelAwareSpecificationRepository<ModifierGroupEntity, Long> {

    List<ModifierGroupEntity> findByIdInAndHotelId(Collection<Long> ids, Long hotelId);
}
