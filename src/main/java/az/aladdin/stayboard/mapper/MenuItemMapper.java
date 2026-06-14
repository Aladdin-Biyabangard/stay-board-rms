package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.MenuCategoryEntity;
import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.MenuItemModifierGroupEntity;
import az.aladdin.stayboard.model.enums.SaleUnitType;
import az.aladdin.stayboard.model.request.CreateMenuItemRequest;
import az.aladdin.stayboard.model.request.PatchMenuItemRequest;
import az.aladdin.stayboard.model.request.UpdateMenuItemRequest;
import az.aladdin.stayboard.model.response.AllergenResponse;
import az.aladdin.stayboard.model.response.DietaryTagResponse;
import az.aladdin.stayboard.model.response.MenuItemResponse;
import az.aladdin.stayboard.model.response.ModifierGroupResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import az.aladdin.stayboard.service.menu.ModifierOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MenuItemMapper {

    private final HotelTimeService hotelTimeService;
    private final AllergenMapper allergenMapper;
    private final DietaryTagMapper dietaryTagMapper;
    private final ModifierGroupMapper modifierGroupMapper;
    private final ModifierOptionService modifierOptionService;

    public MenuItemEntity toEntity(CreateMenuItemRequest request, Long hotelId, MenuCategoryEntity menuCategory) {
        return MenuItemEntity.builder()
                .hotelId(hotelId)
                .itemName(request.itemName())
                .itemDescription(request.itemDescription())
                .active(request.active())
                .price(request.price())
                .taxRate(request.taxRate())
                .taxType(request.taxType())
                .saleUnitType(request.saleUnitType() != null ? request.saleUnitType() : SaleUnitType.PIECE)
                .menuCategory(menuCategory)
                .build();
    }

    public void updateEntity(MenuItemEntity entity, UpdateMenuItemRequest request, MenuCategoryEntity menuCategory) {
        entity.setItemName(request.itemName());
        entity.setItemDescription(request.itemDescription());
        entity.setActive(request.active());
        entity.setPrice(request.price());
        entity.setTaxRate(request.taxRate());
        entity.setTaxType(request.taxType());
        entity.setSaleUnitType(request.saleUnitType());
        entity.setMenuCategory(menuCategory);
    }

    public void patchEntity(MenuItemEntity entity, PatchMenuItemRequest request, MenuCategoryEntity menuCategory) {
        if (request.itemName() != null) {
            entity.setItemName(request.itemName());
        }
        if (request.itemDescription() != null) {
            entity.setItemDescription(request.itemDescription());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        if (request.price() != null) {
            entity.setPrice(request.price());
        }
        if (request.taxRate() != null) {
            entity.setTaxRate(request.taxRate());
        }
        if (request.taxType() != null) {
            entity.setTaxType(request.taxType());
        }
        if (request.saleUnitType() != null) {
            entity.setSaleUnitType(request.saleUnitType());
        }
        if (menuCategory != null) {
            entity.setMenuCategory(menuCategory);
        }
    }

    public MenuItemResponse toResponse(MenuItemEntity entity) {
        Long hotelId = entity.getHotelId();

        List<AllergenResponse> allergens = entity.getAllergens().stream()
                .sorted(Comparator.comparing(a -> a.getAllergenName() != null ? a.getAllergenName() : ""))
                .map(allergenMapper::toResponse)
                .toList();

        List<DietaryTagResponse> dietaryTags = entity.getDietaryTags().stream()
                .sorted(Comparator.comparing(t -> t.getTagName() != null ? t.getTagName() : ""))
                .map(dietaryTagMapper::toResponse)
                .toList();

        List<ModifierGroupResponse> modifierGroups = entity.getModifierGroupLinks().stream()
                .sorted(Comparator.comparingInt(MenuItemModifierGroupEntity::getSortOrder))
                .map(link -> modifierGroupMapper.toResponse(
                        link.getModifierGroup(),
                        modifierOptionService.listActiveByGroupId(link.getModifierGroup().getId())
                ))
                .filter(group -> group.active())
                .toList();

        return new MenuItemResponse(
                entity.getId(),
                hotelId,
                entity.getItemName(),
                entity.getItemDescription(),
                entity.isActive(),
                entity.getPrice(),
                entity.getTaxRate(),
                entity.getTaxType(),
                entity.getSaleUnitType(),
                entity.getMenuCategory() != null ? entity.getMenuCategory().getId() : null,
                entity.getMenuCategory() != null ? entity.getMenuCategory().getCategoryName() : null,
                entity.getPhotoUrls(),
                entity.getMainImageUrl(),
                allergens,
                dietaryTags,
                modifierGroups,
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy()
        );
    }
}
