package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.model.request.CreateOrderItemRequest;
import az.aladdin.stayboard.model.request.PatchOrderItemRequest;
import az.aladdin.stayboard.model.request.UpdateOrderItemRequest;
import az.aladdin.stayboard.model.response.OrderItemResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {

    private final HotelTimeService hotelTimeService;

    public OrderItemEntity toEntity(
            CreateOrderItemRequest request,
            Long hotelId,
            OrderEntity order,
            MenuItemEntity menuItem
    ) {
        return OrderItemEntity.builder()
                .hotelId(hotelId)
                .order(order)
                .menuItem(menuItem)
                .quantity(request.quantity())
                .weightQuantity(request.weightQuantity())
                .netAmount(request.netAmount())
                .taxAmount(request.taxAmount())
                .grossAmount(request.grossAmount())
                .taxRate(request.taxRate())
                .taxType(request.taxType())
                .orderItemStatus(request.orderItemStatus())
                .build();
    }

    public void updateEntity(
            OrderItemEntity entity,
            UpdateOrderItemRequest request,
            OrderEntity order,
            MenuItemEntity menuItem
    ) {
        entity.setOrder(order);
        entity.setMenuItem(menuItem);
        entity.setQuantity(request.quantity());
        entity.setWeightQuantity(request.weightQuantity());
        entity.setNetAmount(request.netAmount());
        entity.setTaxAmount(request.taxAmount());
        entity.setGrossAmount(request.grossAmount());
        entity.setTaxRate(request.taxRate());
        entity.setTaxType(request.taxType());
        entity.setOrderItemStatus(request.orderItemStatus());
    }

    public void patchEntity(
            OrderItemEntity entity,
            PatchOrderItemRequest request,
            OrderEntity order,
            MenuItemEntity menuItem
    ) {
        if (order != null) {
            entity.setOrder(order);
        }
        if (menuItem != null) {
            entity.setMenuItem(menuItem);
        }
        if (request.quantity() != null) {
            entity.setQuantity(request.quantity());
        }
        if (request.weightQuantity() != null) {
            entity.setWeightQuantity(request.weightQuantity());
        }
        if (request.netAmount() != null) {
            entity.setNetAmount(request.netAmount());
        }
        if (request.taxAmount() != null) {
            entity.setTaxAmount(request.taxAmount());
        }
        if (request.grossAmount() != null) {
            entity.setGrossAmount(request.grossAmount());
        }
        if (request.taxRate() != null) {
            entity.setTaxRate(request.taxRate());
        }
        if (request.taxType() != null) {
            entity.setTaxType(request.taxType());
        }
        if (request.orderItemStatus() != null) {
            entity.setOrderItemStatus(request.orderItemStatus());
        }
    }

    public OrderItemResponse toResponse(OrderItemEntity entity) {
        Long hotelId = entity.getHotelId();
        return new OrderItemResponse(
                entity.getId(),
                hotelId,
                entity.getOrder() != null ? entity.getOrder().getId() : null,
                entity.getOrder() != null ? entity.getOrder().getOrderNumber() : null,
                entity.getMenuItem() != null ? entity.getMenuItem().getId() : null,
                entity.getMenuItem() != null ? entity.getMenuItem().getItemName() : null,
                entity.getQuantity(),
                entity.getWeightQuantity(),
                entity.getNetAmount(),
                entity.getTaxAmount(),
                entity.getGrossAmount(),
                entity.getTaxRate(),
                entity.getTaxType(),
                entity.getOrderItemStatus(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy(),
                hotelTimeService.resolveHotelTimezone(hotelId)
        );
    }
}
