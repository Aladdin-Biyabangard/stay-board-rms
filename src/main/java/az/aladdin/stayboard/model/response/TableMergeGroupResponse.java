package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.TableEntity;

import java.util.List;

public record TableMergeGroupResponse(
        String mergeGroupId,
        Long primaryTableId,
        List<TableSummary> tables,
        List<OrderSummary> activeOrders
) {

    public record TableSummary(
            Long id,
            String tableNumber,
            int capacity,
            int maxCapacity,
            boolean mergeable
    ) {
        public static TableSummary from(TableEntity table) {
            return new TableSummary(
                    table.getId(),
                    table.getTableNumber(),
                    table.getCapacity(),
                    table.getMaxCapacity(),
                    table.isMergeable()
            );
        }
    }

    public record OrderSummary(
            Long id,
            String orderNumber,
            Long tableId,
            String tableNumber
    ) {
        public static OrderSummary from(OrderEntity order) {
            return new OrderSummary(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getTableEntity().getId(),
                    order.getTableEntity().getTableNumber()
            );
        }
    }
}
