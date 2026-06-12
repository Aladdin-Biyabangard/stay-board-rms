package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;

import java.util.Collection;
import java.util.List;

public interface TableRepository extends HotelAwareSpecificationRepository<TableEntity, Long> {

    List<TableEntity> findByIdInAndHotelId(Collection<Long> ids, Long hotelId);

    List<TableEntity> findByMergeGroupIdAndHotelId(String mergeGroupId, Long hotelId);

    List<TableEntity> findByHotelIdAndMergeGroupIdIsNotNull(Long hotelId);
}
