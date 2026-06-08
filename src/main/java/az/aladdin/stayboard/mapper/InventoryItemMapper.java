package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.InventoryItemEntity;
import az.aladdin.stayboard.model.request.CreateInventoryItemRequest;
import az.aladdin.stayboard.model.request.PatchInventoryItemRequest;
import az.aladdin.stayboard.model.request.UpdateInventoryItemRequest;
import az.aladdin.stayboard.model.response.InventoryItemResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class InventoryItemMapper {

    private final HotelTimeService hotelTimeService;

    public InventoryItemEntity toEntity(CreateInventoryItemRequest request, Long hotelId) {
        return InventoryItemEntity.builder()
                .hotelId(hotelId)
                .name(request.name())
                .sku(request.sku())
                .description(request.description())
                .active(request.active() == null || request.active())
                .unitType(request.unitType())
                .unitOfMeasure(request.unitOfMeasure())
                .currentStock(request.currentStock() != null ? request.currentStock() : BigDecimal.ZERO)
                .reorderLevel(request.reorderLevel())
                .build();
    }

    public void updateEntity(InventoryItemEntity entity, UpdateInventoryItemRequest request) {
        entity.setName(request.name());
        entity.setSku(request.sku());
        entity.setDescription(request.description());
        entity.setActive(request.active());
        entity.setUnitType(request.unitType());
        entity.setUnitOfMeasure(request.unitOfMeasure());
        entity.setReorderLevel(request.reorderLevel());
    }

    public void patchEntity(InventoryItemEntity entity, PatchInventoryItemRequest request) {
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.sku() != null) {
            entity.setSku(request.sku());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        if (request.unitType() != null) {
            entity.setUnitType(request.unitType());
        }
        if (request.unitOfMeasure() != null) {
            entity.setUnitOfMeasure(request.unitOfMeasure());
        }
        if (request.reorderLevel() != null) {
            entity.setReorderLevel(request.reorderLevel());
        }
    }

    public InventoryItemResponse toResponse(InventoryItemEntity entity) {
        Long hotelId = entity.getHotelId();
        boolean lowStock = entity.getReorderLevel() != null
                && entity.getCurrentStock().compareTo(entity.getReorderLevel()) <= 0;
        return new InventoryItemResponse(
                entity.getId(),
                hotelId,
                entity.getName(),
                entity.getSku(),
                entity.getDescription(),
                entity.isActive(),
                entity.getUnitType(),
                entity.getUnitOfMeasure(),
                entity.getCurrentStock(),
                entity.getReorderLevel(),
                lowStock,
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy()
        );
    }
}
