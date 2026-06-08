package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.model.response.KitchenTicketResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KitchenTicketMapper {

    private final HotelTimeService hotelTimeService;

    public KitchenTicketResponse toResponse(OrderItemEntity entity) {
        OrderEntity order = entity.getOrder();
        TableEntity table = order != null ? order.getTableEntity() : null;
        return new KitchenTicketResponse(
                entity.getId(),
                order != null ? order.getId() : null,
                order != null ? order.getOrderNumber() : null,
                table != null ? table.getId() : null,
                table != null ? table.getTableNumber() : null,
                order != null ? order.getRoomNumber() : null,
                entity.getMenuItem() != null ? entity.getMenuItem().getId() : null,
                entity.getMenuItem() != null ? entity.getMenuItem().getItemName() : null,
                entity.getMenuItem() != null ? entity.getMenuItem().getSaleUnitType() : null,
                entity.getQuantity(),
                entity.getWeightQuantity(),
                entity.getOrderItemStatus(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), entity.getHotelId())
        );
    }
}
