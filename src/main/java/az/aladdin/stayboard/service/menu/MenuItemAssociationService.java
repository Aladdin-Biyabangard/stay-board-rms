package az.aladdin.stayboard.service.menu;

import az.aladdin.stayboard.entity.AllergenEntity;
import az.aladdin.stayboard.entity.DietaryTagEntity;
import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.MenuItemModifierGroupEntity;
import az.aladdin.stayboard.entity.ModifierGroupEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.repository.AllergenRepository;
import az.aladdin.stayboard.repository.DietaryTagRepository;
import az.aladdin.stayboard.repository.ModifierGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemAssociationService {

    private final AllergenRepository allergenRepository;
    private final DietaryTagRepository dietaryTagRepository;
    private final ModifierGroupRepository modifierGroupRepository;

    public void syncAllergens(MenuItemEntity entity, List<Long> allergenIds, Long hotelId) {
        if (allergenIds == null) {
            return;
        }
        Set<AllergenEntity> allergens = new HashSet<>(allergenRepository.findByIdInAndHotelId(allergenIds, hotelId));
        if (allergens.size() != allergenIds.size()) {
            throw ApiExceptions.notFound(EntityKey.ALLERGEN);
        }
        entity.getAllergens().clear();
        entity.getAllergens().addAll(allergens);
    }

    public void syncDietaryTags(MenuItemEntity entity, List<Long> dietaryTagIds, Long hotelId) {
        if (dietaryTagIds == null) {
            return;
        }
        Set<DietaryTagEntity> tags = new HashSet<>(dietaryTagRepository.findByIdInAndHotelId(dietaryTagIds, hotelId));
        if (tags.size() != dietaryTagIds.size()) {
            throw ApiExceptions.notFound(EntityKey.DIETARY_TAG);
        }
        entity.getDietaryTags().clear();
        entity.getDietaryTags().addAll(tags);
    }

    public void syncModifierGroups(MenuItemEntity entity, List<Long> modifierGroupIds, Long hotelId) {
        if (modifierGroupIds == null) {
            return;
        }

        List<Long> normalizedGroupIds = new ArrayList<>(new LinkedHashSet<>(modifierGroupIds));
        List<ModifierGroupEntity> groups = modifierGroupRepository.findByIdInAndHotelId(normalizedGroupIds, hotelId);
        if (groups.size() != normalizedGroupIds.size()) {
            throw ApiExceptions.notFound(EntityKey.MODIFIER_GROUP);
        }

        Map<Long, ModifierGroupEntity> groupById = groups.stream()
                .collect(Collectors.toMap(ModifierGroupEntity::getId, Function.identity()));

        Set<Long> desiredGroupIds = new HashSet<>(normalizedGroupIds);
        entity.getModifierGroupLinks().removeIf(link -> !desiredGroupIds.contains(link.getModifierGroup().getId()));

        for (int i = 0; i < normalizedGroupIds.size(); i++) {
            final int sortOrder = i;
            Long groupId = normalizedGroupIds.get(i);
            ModifierGroupEntity group = groupById.get(groupId);

            MenuItemModifierGroupEntity link = entity.getModifierGroupLinks().stream()
                    .filter(existing -> existing.getModifierGroup().getId().equals(groupId))
                    .findFirst()
                    .orElseGet(() -> {
                        MenuItemModifierGroupEntity created = MenuItemModifierGroupEntity.builder()
                                .hotelId(hotelId)
                                .menuItem(entity)
                                .modifierGroup(group)
                                .sortOrder(sortOrder)
                                .build();
                        entity.getModifierGroupLinks().add(created);
                        return created;
                    });
            link.setSortOrder(sortOrder);
        }
    }
}
