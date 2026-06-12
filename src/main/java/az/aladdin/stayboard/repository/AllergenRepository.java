package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.AllergenEntity;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;

import java.util.Collection;
import java.util.List;

public interface AllergenRepository extends HotelAwareSpecificationRepository<AllergenEntity, Long> {

    List<AllergenEntity> findByIdInAndHotelId(Collection<Long> ids, Long hotelId);
}
