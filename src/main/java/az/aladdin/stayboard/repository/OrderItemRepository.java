package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long>, JpaSpecificationExecutor<OrderItemEntity> {

    Optional<OrderItemEntity> findByIdAndHotelId(Long id, Long hotelId);

    boolean existsByOrder_IdAndHotelId(Long orderId, Long hotelId);

    List<OrderItemEntity> findAllByOrder_IdAndHotelId(Long orderId, Long hotelId);
}
