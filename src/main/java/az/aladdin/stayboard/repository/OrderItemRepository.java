package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;

import java.util.List;

public interface OrderItemRepository extends HotelAwareSpecificationRepository<OrderItemEntity, Long> {

    boolean existsByOrder_IdAndHotelId(Long orderId, Long hotelId);

    List<OrderItemEntity> findAllByOrder_IdAndHotelId(Long orderId, Long hotelId);
}
