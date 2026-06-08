package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TableRepository extends JpaRepository<TableEntity, Long>, JpaSpecificationExecutor<TableEntity> {

    Optional<TableEntity> findByIdAndHotelId(Long id, Long hotelId);

    List<TableEntity> findByIdInAndHotelId(Collection<Long> ids, Long hotelId);

    List<TableEntity> findByMergeGroupIdAndHotelId(String mergeGroupId, Long hotelId);

    List<TableEntity> findByHotelIdAndMergeGroupIdIsNotNull(Long hotelId);
}
