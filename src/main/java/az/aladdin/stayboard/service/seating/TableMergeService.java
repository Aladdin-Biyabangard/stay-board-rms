package az.aladdin.stayboard.service.seating;

import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.model.enums.OrderStatus;
import az.aladdin.stayboard.model.request.MergeTablesRequest;
import az.aladdin.stayboard.model.response.TableMergeGroupResponse;
import az.aladdin.stayboard.repository.OrderRepository;
import az.aladdin.stayboard.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableMergeService extends HotelAwareService {

    private static final List<OrderStatus> TERMINAL_ORDER_STATUSES = List.of(
            OrderStatus.COMPLETED,
            OrderStatus.CANCELLED
    );

    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public TableMergeGroupResponse mergeTables(MergeTablesRequest request) {
        Long hotelId = getCurrentHotelId();
        Set<Long> requestedIds = new HashSet<>(request.tableIds());

        if (!requestedIds.contains(request.primaryTableId())) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_PRIMARY_TABLE_NOT_IN_GROUP);
        }

        List<TableEntity> tables = tableRepository.findByIdInAndHotelId(requestedIds, hotelId);
        if (tables.size() != requestedIds.size()) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_TABLES_NOT_FOUND);
        }

        if (tables.stream().anyMatch(table -> !table.isMergeable())) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_TABLE_NOT_MERGEABLE);
        }

        List<TableEntity> groupTables = expandWithExistingMergeGroups(hotelId, tables);
        String mergeGroupId = resolveMergeGroupId(groupTables);
        applyMergeGroup(groupTables, mergeGroupId, request.primaryTableId());
        tableRepository.saveAll(groupTables);

        return buildGroupResponse(mergeGroupId, request.primaryTableId(), groupTables);
    }

    @Transactional
    public void unmergeTables(Long tableId) {
        Long hotelId = getCurrentHotelId();
        TableEntity table = tableRepository.findByIdAndHotelId(tableId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.TABLE));

        if (table.getMergeGroupId() == null) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_TABLE_NOT_IN_MERGE_GROUP);
        }

        List<TableEntity> groupTables = tableRepository.findByMergeGroupIdAndHotelId(
                table.getMergeGroupId(), hotelId
        );
        groupTables.forEach(this::clearMergeState);
        tableRepository.saveAll(groupTables);
    }

    @Transactional(readOnly = true)
    public TableMergeGroupResponse getMergeGroup(Long tableId) {
        Long hotelId = getCurrentHotelId();
        TableEntity table = tableRepository.findByIdAndHotelId(tableId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.TABLE));

        if (table.getMergeGroupId() == null) {
            return buildGroupResponse(null, table.getId(), List.of(table));
        }

        List<TableEntity> groupTables = tableRepository.findByMergeGroupIdAndHotelId(
                table.getMergeGroupId(), hotelId
        );
        return buildGroupResponse(table.getMergeGroupId(), table.getPrimaryTableId(), groupTables);
    }

    private List<TableEntity> expandWithExistingMergeGroups(Long hotelId, List<TableEntity> tables) {
        Set<String> existingGroupIds = tables.stream()
                .map(TableEntity::getMergeGroupId)
                .filter(groupId -> groupId != null && !groupId.isBlank())
                .collect(Collectors.toSet());

        if (existingGroupIds.isEmpty()) {
            return tables;
        }

        Set<Long> tableIds = new HashSet<>();
        tables.forEach(table -> tableIds.add(table.getId()));

        for (String groupId : existingGroupIds) {
            tableRepository.findByMergeGroupIdAndHotelId(groupId, hotelId)
                    .forEach(table -> tableIds.add(table.getId()));
        }

        return tableRepository.findByIdInAndHotelId(tableIds, hotelId);
    }

    private String resolveMergeGroupId(List<TableEntity> tables) {
        return tables.stream()
                .map(TableEntity::getMergeGroupId)
                .filter(groupId -> groupId != null && !groupId.isBlank())
                .findFirst()
                .orElse(UUID.randomUUID().toString());
    }

    private void applyMergeGroup(List<TableEntity> tables, String mergeGroupId, Long primaryTableId) {
        tables.forEach(table -> {
            table.setMergeGroupId(mergeGroupId);
            table.setPrimaryTableId(primaryTableId);
        });
    }

    private void clearMergeState(TableEntity table) {
        table.setMergeGroupId(null);
        table.setPrimaryTableId(null);
    }

    private TableMergeGroupResponse buildGroupResponse(
            String mergeGroupId,
            Long primaryTableId,
            List<TableEntity> tables
    ) {
        Long hotelId = getCurrentHotelId();
        List<Long> tableIds = tables.stream().map(TableEntity::getId).toList();

        List<OrderEntity> activeOrders = orderRepository.findActiveOrdersByTableIds(
                hotelId,
                tableIds,
                TERMINAL_ORDER_STATUSES
        );

        return new TableMergeGroupResponse(
                mergeGroupId,
                primaryTableId,
                tables.stream().map(TableMergeGroupResponse.TableSummary::from).toList(),
                activeOrders.stream().map(TableMergeGroupResponse.OrderSummary::from).toList()
        );
    }
}
