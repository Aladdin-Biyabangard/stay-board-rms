package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.ModifierGroupEntity;
import az.aladdin.stayboard.entity.ModifierOptionEntity;
import az.aladdin.stayboard.model.request.CreateModifierOptionRequest;
import az.aladdin.stayboard.model.request.PatchModifierOptionRequest;
import az.aladdin.stayboard.model.response.ModifierOptionResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ModifierOptionMapper {

    private final HotelTimeService hotelTimeService;

    public ModifierOptionEntity toEntity(CreateModifierOptionRequest request, Long hotelId, ModifierGroupEntity group) {
        return ModifierOptionEntity.builder()
                .hotelId(hotelId)
                .modifierGroup(group)
                .optionName(request.optionName())
                .priceDelta(request.priceDelta() != null ? request.priceDelta() : BigDecimal.ZERO)
                .defaultSelected(request.defaultSelected())
                .active(request.active())
                .sortOrder(request.sortOrder())
                .build();
    }

    public void updateEntity(ModifierOptionEntity entity, CreateModifierOptionRequest request, ModifierGroupEntity group) {
        entity.setModifierGroup(group);
        entity.setOptionName(request.optionName());
        entity.setPriceDelta(request.priceDelta() != null ? request.priceDelta() : BigDecimal.ZERO);
        entity.setDefaultSelected(request.defaultSelected());
        entity.setActive(request.active());
        entity.setSortOrder(request.sortOrder());
    }

    public void patchEntity(ModifierOptionEntity entity, PatchModifierOptionRequest request, ModifierGroupEntity group) {
        if (group != null) {
            entity.setModifierGroup(group);
        }
        if (request.optionName() != null) {
            entity.setOptionName(request.optionName());
        }
        if (request.priceDelta() != null) {
            entity.setPriceDelta(request.priceDelta());
        }
        if (request.defaultSelected() != null) {
            entity.setDefaultSelected(request.defaultSelected());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        if (request.sortOrder() != null) {
            entity.setSortOrder(request.sortOrder());
        }
    }

    public ModifierOptionResponse toResponse(ModifierOptionEntity entity) {
        Long hotelId = entity.getHotelId();
        return new ModifierOptionResponse(
                entity.getId(),
                hotelId,
                entity.getModifierGroup() != null ? entity.getModifierGroup().getId() : null,
                entity.getOptionName(),
                entity.getPriceDelta(),
                entity.isDefaultSelected(),
                entity.isActive(),
                entity.getSortOrder(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy()
        );
    }
}
