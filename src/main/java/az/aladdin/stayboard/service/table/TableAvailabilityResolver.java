package az.aladdin.stayboard.service.table;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.TableOccupancyEntity;
import az.aladdin.stayboard.model.enums.OccupancySourceType;
import az.aladdin.stayboard.model.enums.TableAvailabilityStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static az.aladdin.stayboard.service.TableAvailabilityService.DEFAULT_DINING_DURATION_MINUTES;

@Component
public class TableAvailabilityResolver {

    public boolean overlapsWithOrder(OrderEntity order, LocalDateTime startUtc, LocalDateTime endUtc) {
        if (order.getCreatedAt() == null) {
            return true;
        }
        LocalDateTime orderEnd = order.getCreatedAt().plusMinutes(DEFAULT_DINING_DURATION_MINUTES);
        return order.getCreatedAt().isBefore(endUtc) && orderEnd.isAfter(startUtc);
    }

    public TableAvailabilitySnapshot resolveSnapshot(
            List<Long> groupTableIds,
            LocalDateTime atUtc,
            LocalDateTime windowEndUtc,
            List<TableOccupancyEntity> occupancies,
            List<TableOccupancyEntity> activeNow,
            List<OrderEntity> activeOrders
    ) {
        TableOccupancyEntity currentOccupancy = activeNow.stream()
                .filter(item -> groupTableIds.contains(item.getRestaurantTable().getId()))
                .min(Comparator.comparing(TableOccupancyEntity::getEndDateTime))
                .orElse(null);

        OrderEntity currentOrder = activeOrders.stream()
                .filter(order -> order.getTableEntity() != null
                        && groupTableIds.contains(order.getTableEntity().getId()))
                .findFirst()
                .orElse(null);

        TableAvailabilityStatus status = TableAvailabilityStatus.FREE;
        LocalDateTime statusUntilUtc = null;
        Long currentOccupancyId = null;

        if (currentOccupancy != null) {
            status = toAvailabilityStatus(currentOccupancy.getSourceType());
            statusUntilUtc = currentOccupancy.getEndDateTime();
            currentOccupancyId = currentOccupancy.getId();
        } else if (currentOrder != null && currentOrder.getCreatedAt() != null) {
            status = TableAvailabilityStatus.OCCUPIED;
            statusUntilUtc = currentOrder.getCreatedAt().plusMinutes(DEFAULT_DINING_DURATION_MINUTES);
        }

        boolean windowConflict = occupancies.stream()
                .anyMatch(item -> groupTableIds.contains(item.getRestaurantTable().getId()));
        boolean orderConflict = activeOrders.stream()
                .anyMatch(order -> overlapsWithOrder(order, atUtc, windowEndUtc)
                        && order.getTableEntity() != null
                        && groupTableIds.contains(order.getTableEntity().getId()));

        LocalDateTime nextAvailableAtUtc = resolveNextAvailableAt(atUtc, groupTableIds, occupancies, activeOrders);

        return new TableAvailabilitySnapshot(
                status,
                statusUntilUtc,
                nextAvailableAtUtc,
                !windowConflict && !orderConflict,
                currentOccupancyId
        );
    }

    private LocalDateTime resolveNextAvailableAt(
            LocalDateTime atUtc,
            List<Long> groupTableIds,
            List<TableOccupancyEntity> occupancies,
            List<OrderEntity> activeOrders
    ) {
        List<LocalDateTime> blockEnds = new ArrayList<>();
        occupancies.stream()
                .filter(item -> groupTableIds.contains(item.getRestaurantTable().getId()))
                .filter(item -> item.getEndDateTime().isAfter(atUtc))
                .map(TableOccupancyEntity::getEndDateTime)
                .forEach(blockEnds::add);
        activeOrders.stream()
                .filter(order -> order.getTableEntity() != null
                        && groupTableIds.contains(order.getTableEntity().getId()))
                .filter(order -> order.getCreatedAt() != null)
                .map(order -> order.getCreatedAt().plusMinutes(DEFAULT_DINING_DURATION_MINUTES))
                .filter(end -> end.isAfter(atUtc))
                .forEach(blockEnds::add);

        return blockEnds.stream().min(LocalDateTime::compareTo).orElse(atUtc);
    }

    private TableAvailabilityStatus toAvailabilityStatus(OccupancySourceType sourceType) {
        return switch (sourceType) {
            case RESERVED -> TableAvailabilityStatus.RESERVED;
            case OCCUPIED -> TableAvailabilityStatus.OCCUPIED;
            case OUT_OF_SERVICE -> TableAvailabilityStatus.OUT_OF_SERVICE;
        };
    }

    public record TableAvailabilitySnapshot(
            TableAvailabilityStatus status,
            LocalDateTime statusUntilUtc,
            LocalDateTime nextAvailableAtUtc,
            boolean reservable,
            Long currentOccupancyId
    ) {
    }
}
