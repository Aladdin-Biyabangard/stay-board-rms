package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.ModifierGroupEntity;
import az.aladdin.stayboard.model.request.CreateModifierGroupRequest;
import az.aladdin.stayboard.model.request.PatchModifierGroupRequest;
import az.aladdin.stayboard.model.response.ModifierGroupResponse;
import az.aladdin.stayboard.model.response.ModifierOptionResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ModifierGroupMapper {

    private final HotelTimeService hotelTimeService;

    public ModifierGroupEntity toEntity(CreateModifierGroupRequest request, Long hotelId) {
        return ModifierGroupEntity.builder()
                .hotelId(hotelId)
                .groupName(request.groupName())
                .required(request.required())
                .minSelections(request.minSelections())
                .maxSelections(request.maxSelections())
                .active(request.active())
                .sortOrder(request.sortOrder())
                .priceDelta(normalizePriceDelta(request.priceDelta()))
                .build();
    }

    public void updateEntity(ModifierGroupEntity entity, CreateModifierGroupRequest request) {
        entity.setGroupName(request.groupName());
        entity.setRequired(request.required());
        entity.setMinSelections(request.minSelections());
        entity.setMaxSelections(request.maxSelections());
        entity.setActive(request.active());
        entity.setSortOrder(request.sortOrder());
        entity.setPriceDelta(normalizePriceDelta(request.priceDelta()));
    }

    public void patchEntity(ModifierGroupEntity entity, PatchModifierGroupRequest request) {
        if (request.groupName() != null) {
            entity.setGroupName(request.groupName());
        }
        if (request.required() != null) {
            entity.setRequired(request.required());
        }
        if (request.minSelections() != null) {
            entity.setMinSelections(request.minSelections());
        }
        if (request.maxSelections() != null) {
            entity.setMaxSelections(request.maxSelections());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        if (request.sortOrder() != null) {
            entity.setSortOrder(request.sortOrder());
        }
        if (request.priceDelta() != null) {
            entity.setPriceDelta(normalizePriceDelta(request.priceDelta()));
        }
    }

    public ModifierGroupResponse toResponse(ModifierGroupEntity entity) {
        return toResponse(entity, List.of());
    }

    public ModifierGroupResponse toResponse(ModifierGroupEntity entity, List<ModifierOptionResponse> options) {
        Long hotelId = entity.getHotelId();
        return new ModifierGroupResponse(
                entity.getId(),
                hotelId,
                entity.getGroupName(),
                entity.isRequired(),
                entity.getMinSelections(),
                entity.getMaxSelections(),
                entity.isActive(),
                entity.getSortOrder(),
                entity.getPriceDelta() != null ? entity.getPriceDelta() : BigDecimal.ZERO,
                options != null ? options : List.of(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy()
        );
    }

    private BigDecimal normalizePriceDelta(BigDecimal priceDelta) {
        return priceDelta != null ? priceDelta : BigDecimal.ZERO;
    }
}
