package az.aladdin.stayboard.service;

import az.aladdin.stayboard.context.HotelContextHolder;
import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.entity.TableOccupancyEntity;
import az.aladdin.stayboard.model.enums.OccupancySourceType;
import az.aladdin.stayboard.model.enums.OrderStatus;
import az.aladdin.stayboard.model.enums.TableAvailabilityStatus;
import az.aladdin.stayboard.model.response.TableAvailabilityResponse;
import az.aladdin.stayboard.repository.OrderRepository;
import az.aladdin.stayboard.repository.TableOccupancyRepository;
import az.aladdin.stayboard.repository.TableRepository;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TableAvailabilityService {

    public static final int DEFAULT_DINING_DURATION_MINUTES = 120;

    private static final List<OrderStatus> TERMINAL_ORDER_STATUSES = List.of(
            OrderStatus.COMPLETED,
            OrderStatus.CANCELLED
    );

    private final TableRepository tableRepository;
    private final TableOccupancyRepository tableOccupancyRepository;
    private final OrderRepository orderRepository;
    private final HotelTimeService hotelTimeService;
    private final GuestReservationWindowService guestReservationWindowService;

    @Transactional(readOnly = true)
    public List<TableAvailabilityResponse> getAvailability(
            LocalDateTime atHotelLocal,
            Integer durationMinutes,
            Integer partySize,
            Long reservationId
    ) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        LocalDateTime at = atHotelLocal != null
                ? atHotelLocal
                : hotelTimeService.nowAtHotel(hotelId);
        int duration = durationMinutes != null && durationMinutes > 0
                ? durationMinutes
                : DEFAULT_DINING_DURATION_MINUTES;
        LocalDateTime windowEnd = at.plusMinutes(duration);
        guestReservationWindowService.ensureWithinStayWindow(reservationId, at, windowEnd);
        LocalDateTime atUtc = hotelTimeService.hotelLocalDateTimeToUtc(at, hotelId);
        LocalDateTime windowEndUtc = hotelTimeService.hotelLocalDateTimeToUtc(windowEnd, hotelId);

        List<TableEntity> tables = tableRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("hotelId"), hotelId)
        );
        if (tables.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Long>> tableIdsByGroup = buildTableGroupMap(tables);
        Set<Long> allTableIds = new HashSet<>();
        tableIdsByGroup.values().forEach(allTableIds::addAll);

        List<TableOccupancyEntity> occupancies = tableOccupancyRepository.findOverlappingByTableIds(
                hotelId, allTableIds, atUtc, windowEndUtc
        );
        List<TableOccupancyEntity> activeNow = tableOccupancyRepository.findActiveAt(hotelId, allTableIds, atUtc);
        List<OrderEntity> activeOrders = orderRepository.findActiveOrdersByTableIds(
                hotelId, allTableIds, TERMINAL_ORDER_STATUSES
        );

        List<TableAvailabilityResponse> responses = new ArrayList<>();
        for (TableEntity table : tables) {
            if (partySize != null && table.getMaxCapacity() < partySize) {
                continue;
            }

            List<Long> groupTableIds = tableIdsByGroup.getOrDefault(table.getId(), List.of(table.getId()));
            TableAvailabilitySnapshot snapshot = resolveSnapshot(
                    table.getId(),
                    groupTableIds,
                    atUtc,
                    windowEndUtc,
                    occupancies,
                    activeNow,
                    activeOrders,
                    hotelId
            );

            responses.add(new TableAvailabilityResponse(
                    table.getId(),
                    table.getTableNumber(),
                    table.getCapacity(),
                    table.getMaxCapacity(),
                    table.getAmenities(),
                    snapshot.status(),
                    hotelTimeService.utcLocalDateTimeToHotelLocal(snapshot.statusUntilUtc(), hotelId),
                    hotelTimeService.utcLocalDateTimeToHotelLocal(snapshot.nextAvailableAtUtc(), hotelId),
                    snapshot.reservable(),
                    snapshot.currentOccupancyId()
            ));
        }

        responses.sort(Comparator.comparing(TableAvailabilityResponse::tableNumber));
        return responses;
    }

    public boolean isTableReservable(
            Long hotelId,
            TableEntity table,
            LocalDateTime startUtc,
            LocalDateTime endUtc,
            Integer partySize
    ) {
        if (partySize != null && table.getMaxCapacity() < partySize) {
            return false;
        }
        List<Long> groupTableIds = resolveGroupTableIds(hotelId, table);
        List<TableOccupancyEntity> overlaps = tableOccupancyRepository.findOverlappingByTableIds(
                hotelId, groupTableIds, startUtc, endUtc
        );
        if (!overlaps.isEmpty()) {
            return false;
        }
        List<OrderEntity> activeOrders = orderRepository.findActiveOrdersByTableIds(
                hotelId, groupTableIds, TERMINAL_ORDER_STATUSES
        );
        return activeOrders.stream()
                .noneMatch(order -> overlapsWithOrder(order, startUtc, endUtc));
    }

    private boolean overlapsWithOrder(OrderEntity order, LocalDateTime startUtc, LocalDateTime endUtc) {
        if (order.getCreatedAt() == null) {
            return true;
        }
        LocalDateTime orderEnd = order.getCreatedAt().plusMinutes(DEFAULT_DINING_DURATION_MINUTES);
        return order.getCreatedAt().isBefore(endUtc) && orderEnd.isAfter(startUtc);
    }

    private TableAvailabilitySnapshot resolveSnapshot(
            Long tableId,
            List<Long> groupTableIds,
            LocalDateTime atUtc,
            LocalDateTime windowEndUtc,
            List<TableOccupancyEntity> occupancies,
            List<TableOccupancyEntity> activeNow,
            List<OrderEntity> activeOrders,
            Long hotelId
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

        LocalDateTime nextAvailableAtUtc = resolveNextAvailableAt(
                atUtc,
                groupTableIds,
                occupancies,
                activeOrders
        );

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

    private Map<Long, List<Long>> buildTableGroupMap(List<TableEntity> tables) {
        Map<String, List<TableEntity>> grouped = new HashMap<>();
        for (TableEntity table : tables) {
            String key = table.getMergeGroupId() != null
                    ? "group:" + table.getMergeGroupId()
                    : "table:" + table.getId();
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(table);
        }

        Map<Long, List<Long>> result = new HashMap<>();
        for (List<TableEntity> group : grouped.values()) {
            List<Long> ids = group.stream().map(TableEntity::getId).toList();
            for (TableEntity table : group) {
                result.put(table.getId(), ids);
            }
        }
        return result;
    }

    private List<Long> resolveGroupTableIds(Long hotelId, TableEntity table) {
        if (table.getMergeGroupId() == null) {
            return List.of(table.getId());
        }
        return tableRepository.findByMergeGroupIdAndHotelId(table.getMergeGroupId(), hotelId).stream()
                .map(TableEntity::getId)
                .toList();
    }

    private record TableAvailabilitySnapshot(
            TableAvailabilityStatus status,
            LocalDateTime statusUntilUtc,
            LocalDateTime nextAvailableAtUtc,
            boolean reservable,
            Long currentOccupancyId
    ) {
    }
}
