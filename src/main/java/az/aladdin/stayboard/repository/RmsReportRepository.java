package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.enums.OrderStatus;
import az.aladdin.stayboard.repository.projection.RmsCategorySalesProjection;
import az.aladdin.stayboard.repository.projection.RmsOrderChannelCountProjection;
import az.aladdin.stayboard.repository.projection.RmsOrderStatusCountProjection;
import az.aladdin.stayboard.repository.projection.RmsRevenueTotalsProjection;
import az.aladdin.stayboard.repository.projection.RmsTopMenuItemProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface RmsReportRepository extends Repository<OrderEntity, Long> {

    @Query("""
            SELECT
                COALESCE(SUM(oi.grossAmount), 0) AS grossAmount,
                COALESCE(SUM(oi.netAmount), 0) AS netAmount,
                COALESCE(SUM(oi.taxAmount), 0) AS taxAmount,
                COUNT(oi.id) AS itemLineCount,
                COUNT(DISTINCT o.id) AS orderCount
            FROM OrderItemEntity oi
            JOIN oi.order o
            WHERE oi.hotelId = :hotelId
              AND o.orderStatus <> az.aladdin.stayboard.model.enums.OrderStatus.CANCELLED
              AND oi.orderItemStatus <> az.aladdin.stayboard.model.enums.OrderItemStatus.CANCELLED
              AND o.createdAt >= :startUtc
              AND o.createdAt < :endUtcExclusive
            """)
    RmsRevenueTotalsProjection sumRevenueTotals(
            @Param("hotelId") Long hotelId,
            @Param("startUtc") LocalDateTime startUtc,
            @Param("endUtcExclusive") LocalDateTime endUtcExclusive
    );

    @Query("""
            SELECT o.orderStatus AS orderStatus, COUNT(o) AS orderCount
            FROM OrderEntity o
            WHERE o.hotelId = :hotelId
              AND o.createdAt >= :startUtc
              AND o.createdAt < :endUtcExclusive
            GROUP BY o.orderStatus
            """)
    List<RmsOrderStatusCountProjection> countOrdersByStatus(
            @Param("hotelId") Long hotelId,
            @Param("startUtc") LocalDateTime startUtc,
            @Param("endUtcExclusive") LocalDateTime endUtcExclusive
    );

    @Query("""
            SELECT
                CASE WHEN o.roomNumber IS NOT NULL AND TRIM(o.roomNumber) <> '' THEN TRUE ELSE FALSE END AS roomCharge,
                COUNT(o) AS orderCount,
                COALESCE(SUM(o.totalAmount), 0) AS grossAmount
            FROM OrderEntity o
            WHERE o.hotelId = :hotelId
              AND o.orderStatus <> az.aladdin.stayboard.model.enums.OrderStatus.CANCELLED
              AND o.createdAt >= :startUtc
              AND o.createdAt < :endUtcExclusive
            GROUP BY CASE WHEN o.roomNumber IS NOT NULL AND TRIM(o.roomNumber) <> '' THEN TRUE ELSE FALSE END
            """)
    List<RmsOrderChannelCountProjection> countOrdersByChannel(
            @Param("hotelId") Long hotelId,
            @Param("startUtc") LocalDateTime startUtc,
            @Param("endUtcExclusive") LocalDateTime endUtcExclusive
    );

    @Query("""
            SELECT
                mi.id AS menuItemId,
                mi.itemName AS menuItemName,
                COALESCE(mc.categoryName, 'Uncategorized') AS categoryName,
                COALESCE(SUM(oi.quantity), 0) AS quantitySold,
                COALESCE(SUM(oi.grossAmount), 0) AS grossAmount
            FROM OrderItemEntity oi
            JOIN oi.order o
            JOIN oi.menuItem mi
            LEFT JOIN mi.menuCategory mc
            WHERE oi.hotelId = :hotelId
              AND o.orderStatus <> az.aladdin.stayboard.model.enums.OrderStatus.CANCELLED
              AND oi.orderItemStatus <> az.aladdin.stayboard.model.enums.OrderItemStatus.CANCELLED
              AND o.createdAt >= :startUtc
              AND o.createdAt < :endUtcExclusive
            GROUP BY mi.id, mi.itemName, mc.categoryName
            ORDER BY SUM(oi.grossAmount) DESC
            """)
    List<RmsTopMenuItemProjection> findTopMenuItems(
            @Param("hotelId") Long hotelId,
            @Param("startUtc") LocalDateTime startUtc,
            @Param("endUtcExclusive") LocalDateTime endUtcExclusive,
            Pageable pageable
    );

    @Query("""
            SELECT
                mc.id AS categoryId,
                COALESCE(mc.categoryName, 'Uncategorized') AS categoryName,
                COUNT(oi.id) AS itemLineCount,
                COALESCE(SUM(oi.quantity), 0) AS quantitySold,
                COALESCE(SUM(oi.grossAmount), 0) AS grossAmount,
                COALESCE(SUM(oi.netAmount), 0) AS netAmount,
                COALESCE(SUM(oi.taxAmount), 0) AS taxAmount
            FROM OrderItemEntity oi
            JOIN oi.order o
            JOIN oi.menuItem mi
            LEFT JOIN mi.menuCategory mc
            WHERE oi.hotelId = :hotelId
              AND o.orderStatus <> az.aladdin.stayboard.model.enums.OrderStatus.CANCELLED
              AND oi.orderItemStatus <> az.aladdin.stayboard.model.enums.OrderItemStatus.CANCELLED
              AND o.createdAt >= :startUtc
              AND o.createdAt < :endUtcExclusive
            GROUP BY mc.id, mc.categoryName
            ORDER BY SUM(oi.grossAmount) DESC
            """)
    List<RmsCategorySalesProjection> sumSalesByCategory(
            @Param("hotelId") Long hotelId,
            @Param("startUtc") LocalDateTime startUtc,
            @Param("endUtcExclusive") LocalDateTime endUtcExclusive
    );

    @Query("""
            SELECT COUNT(o)
            FROM OrderEntity o
            WHERE o.hotelId = :hotelId
              AND o.orderStatus IN :statuses
            """)
    long countOrdersByStatuses(
            @Param("hotelId") Long hotelId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("""
            SELECT COUNT(oi)
            FROM OrderItemEntity oi
            JOIN oi.order o
            WHERE oi.hotelId = :hotelId
              AND o.orderStatus <> az.aladdin.stayboard.model.enums.OrderStatus.CANCELLED
              AND oi.orderItemStatus IN :statuses
            """)
    long countKitchenTicketsByStatuses(
            @Param("hotelId") Long hotelId,
            @Param("statuses") Collection<OrderItemStatus> statuses
    );

    @Query("""
            SELECT COUNT(o)
            FROM TableOccupancyEntity o
            WHERE o.hotelId = :hotelId
              AND o.startDateTime <= :at
              AND o.endDateTime > :at
            """)
    long countActiveTableOccupancies(
            @Param("hotelId") Long hotelId,
            @Param("at") LocalDateTime at
    );
}
