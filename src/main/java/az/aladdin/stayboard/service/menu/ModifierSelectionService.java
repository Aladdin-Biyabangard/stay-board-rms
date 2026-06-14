package az.aladdin.stayboard.service.menu;

import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.MenuItemModifierGroupEntity;
import az.aladdin.stayboard.entity.ModifierGroupEntity;
import az.aladdin.stayboard.entity.ModifierOptionEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.entity.OrderItemModifierEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.repository.ModifierGroupRepository;
import az.aladdin.stayboard.repository.ModifierOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModifierSelectionService {

    private final ModifierGroupRepository modifierGroupRepository;
    private final ModifierOptionRepository modifierOptionRepository;

    public List<OrderItemModifierEntity> buildOrderItemModifiers(
            OrderItemEntity orderItem,
            MenuItemEntity menuItem,
            Long hotelId,
            List<Long> modifierGroupIds,
            List<Long> modifierOptionIds
    ) {
        if (modifierOptionIds != null && !modifierOptionIds.isEmpty()) {
            return buildFromOptions(orderItem, menuItem, hotelId, modifierOptionIds);
        }
        return buildFromGroups(orderItem, menuItem, hotelId, modifierGroupIds);
    }

    public BigDecimal sumPriceDeltas(List<OrderItemModifierEntity> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return modifiers.stream()
                .map(m -> m.getPriceDelta() != null ? m.getPriceDelta() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderItemModifierEntity> buildFromGroups(
            OrderItemEntity orderItem,
            MenuItemEntity menuItem,
            Long hotelId,
            List<Long> modifierGroupIds
    ) {
        List<Long> requestedIds = modifierGroupIds != null ? modifierGroupIds : List.of();

        List<Long> uniqueIds = requestedIds.stream().distinct().toList();
        List<ModifierGroupEntity> selectedGroups = uniqueIds.isEmpty()
                ? List.of()
                : modifierGroupRepository.findByIdInAndHotelId(uniqueIds, hotelId);

        if (selectedGroups.size() != uniqueIds.size()) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_MODIFIER_GROUP);
        }

        Map<Long, ModifierGroupEntity> selectedById = selectedGroups.stream()
                .collect(Collectors.toMap(ModifierGroupEntity::getId, Function.identity()));

        validateGroupSelections(menuItem, requestedIds);

        return requestedIds.stream()
                .map(selectedById::get)
                .map(group -> OrderItemModifierEntity.builder()
                        .hotelId(hotelId)
                        .orderItem(orderItem)
                        .modifierGroupId(group.getId())
                        .modifierName(group.getGroupName())
                        .priceDelta(group.getPriceDelta() != null ? group.getPriceDelta() : BigDecimal.ZERO)
                        .build())
                .toList();
    }

    private List<OrderItemModifierEntity> buildFromOptions(
            OrderItemEntity orderItem,
            MenuItemEntity menuItem,
            Long hotelId,
            List<Long> modifierOptionIds
    ) {
        List<Long> uniqueOptionIds = modifierOptionIds.stream().distinct().toList();
        List<ModifierOptionEntity> selectedOptions = modifierOptionRepository.findByIdInAndHotelId(uniqueOptionIds, hotelId);

        if (selectedOptions.size() != uniqueOptionIds.size()) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_MODIFIER_OPTION);
        }

        for (ModifierOptionEntity option : selectedOptions) {
            if (!option.isActive()) {
                throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_MODIFIER_OPTION);
            }
        }

        List<Long> groupIdsFromOptions = modifierOptionIds.stream()
                .map(optionId -> selectedOptions.stream()
                        .filter(option -> option.getId().equals(optionId))
                        .findFirst()
                        .map(option -> option.getModifierGroup().getId())
                        .orElseThrow(() -> ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_MODIFIER_OPTION)))
                .toList();

        validateGroupSelections(menuItem, groupIdsFromOptions);

        Map<Long, ModifierOptionEntity> optionById = selectedOptions.stream()
                .collect(Collectors.toMap(ModifierOptionEntity::getId, Function.identity()));

        return modifierOptionIds.stream()
                .map(optionById::get)
                .map(option -> OrderItemModifierEntity.builder()
                        .hotelId(hotelId)
                        .orderItem(orderItem)
                        .modifierGroupId(option.getModifierGroup().getId())
                        .modifierName(option.getOptionName())
                        .priceDelta(option.getPriceDelta() != null ? option.getPriceDelta() : BigDecimal.ZERO)
                        .build())
                .toList();
    }

    private void validateGroupSelections(MenuItemEntity menuItem, List<Long> requestedGroupIds) {
        Map<Long, Long> countByGroup = requestedGroupIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Set<Long> allowedGroupIds = menuItem.getModifierGroupLinks().stream()
                .map(link -> link.getModifierGroup().getId())
                .collect(Collectors.toSet());

        for (Long groupId : countByGroup.keySet()) {
            if (!allowedGroupIds.contains(groupId)) {
                throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_MODIFIER_NOT_ALLOWED);
            }
        }

        for (MenuItemModifierGroupEntity link : menuItem.getModifierGroupLinks()) {
            ModifierGroupEntity group = link.getModifierGroup();
            int count = countByGroup.getOrDefault(group.getId(), 0L).intValue();

            if (group.isRequired() && count < Math.max(1, group.getMinSelections())) {
                throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_MODIFIER_SELECTION_REQUIRED);
            }
            if (count < group.getMinSelections()) {
                throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_MODIFIER_SELECTION_MIN);
            }
            if (group.getMaxSelections() > 0 && count > group.getMaxSelections()) {
                throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_MODIFIER_SELECTION_MAX);
            }
        }
    }
}
