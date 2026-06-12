package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.DietaryTagEntity;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;

import java.util.Collection;
import java.util.List;

public interface DietaryTagRepository extends HotelAwareSpecificationRepository<DietaryTagEntity, Long> {

    List<DietaryTagEntity> findByIdInAndHotelId(Collection<Long> ids, Long hotelId);
}
