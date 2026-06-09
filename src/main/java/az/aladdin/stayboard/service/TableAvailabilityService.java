package az.aladdin.stayboard.service;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.entity.TableOccupancyEntity;
import az.aladdin.stayboard.model.enums.OrderStatus;
import az.aladdin.stayboard.model.response.TableAvailabilityResponse;
import az.aladdin.stayboard.repository.OrderRepository;
import az.aladdin.stayboard.repository.TableOccupancyRepository;
import az.aladdin.stayboard.repository.TableRepository;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import az.aladdin.stayboard.service.table.TableAvailabilityResolver;
import az.aladdin.stayboard.service.table.TableMergeGroupSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TableAvailabilityService extends HotelAwareService {

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
    private final TableMergeGroupSupport tableMergeGroupSupport;
    private final TableAvailabilityResolver tableAvailabilityResolver;

    @Transactional(readOnly = true)
    public List<TableAvailabilityResponse> getAvailability(
            LocalDateTime atHotelLocal,
            Integer durationMinutes,
            Integer partySize,
            Long reservationId
    ) {
        Long hotelId = getCurrentHotelId();
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

        Map<Long, List<Long>> tableIdsByGroup = tableMergeGroupSupport.buildTableGroupMap(tables);
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
            TableAvailabilityResolver.TableAvailabilitySnapshot snapshot = tableAvailabilityResolver.resolveSnapshot(
                    groupTableIds,
                    atUtc,
                    windowEndUtc,
                    occupancies,
                    activeNow,
                    activeOrders
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
        List<Long> groupTableIds = tableMergeGroupSupport.resolveGroupTableIds(hotelId, table);
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
                .noneMatch(order -> tableAvailabilityResolver.overlapsWithOrder(order, startUtc, endUtc));
    }
}
