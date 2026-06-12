package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.TableOccupancyEntity;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TableOccupancyRepository extends HotelAwareSpecificationRepository<TableOccupancyEntity, Long> {

    @Query("""
            SELECT o FROM TableOccupancyEntity o
            WHERE o.hotelId = :hotelId
              AND o.restaurantTable.id IN :tableIds
              AND o.startDateTime < :endDateTime
              AND o.endDateTime > :startDateTime
            ORDER BY o.startDateTime ASC
            """)
    List<TableOccupancyEntity> findOverlappingByTableIds(
            @Param("hotelId") Long hotelId,
            @Param("tableIds") Collection<Long> tableIds,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("""
            SELECT o FROM TableOccupancyEntity o
            WHERE o.hotelId = :hotelId
              AND o.restaurantTable.id IN :tableIds
              AND o.startDateTime <= :at
              AND o.endDateTime > :at
            ORDER BY o.endDateTime ASC
            """)
    List<TableOccupancyEntity> findActiveAt(
            @Param("hotelId") Long hotelId,
            @Param("tableIds") Collection<Long> tableIds,
            @Param("at") LocalDateTime at
    );
}
