package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.OrderItemModifierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemModifierRepository extends JpaRepository<OrderItemModifierEntity, Long> {

    List<OrderItemModifierEntity> findByOrderItem_IdAndHotelId(Long orderItemId, Long hotelId);
}
