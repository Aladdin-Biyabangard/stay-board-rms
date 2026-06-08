package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, Long>, JpaSpecificationExecutor<InventoryItemEntity> {

    Optional<InventoryItemEntity> findByIdAndHotelId(Long id, Long hotelId);
}
