package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    Optional<OrderEntity> findByIdAndHotelId(Long id, Long hotelId);

    boolean existsByTableEntityIdAndHotelId(Long tableEntityId, Long hotelId);

    @Query("""
            SELECT o FROM OrderEntity o
            WHERE o.hotelId = :hotelId
              AND o.tableEntity.id IN :tableIds
              AND o.orderStatus NOT IN :excludedStatuses
            ORDER BY o.createdAt DESC
            """)
    List<OrderEntity> findActiveOrdersByTableIds(
            @Param("hotelId") Long hotelId,
            @Param("tableIds") Collection<Long> tableIds,
            @Param("excludedStatuses") Collection<OrderStatus> excludedStatuses
    );
}
