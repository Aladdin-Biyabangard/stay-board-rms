package az.aladdin.stayboard.service;

import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.entity.InventoryItemEntity;
import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.RecipeEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.mapper.RecipeMapper;
import az.aladdin.stayboard.model.request.CreateRecipeRequest;
import az.aladdin.stayboard.model.request.UpdateRecipeRequest;
import az.aladdin.stayboard.model.request.search.RecipeSearchCriteria;
import az.aladdin.stayboard.model.response.RecipeResponse;
import az.aladdin.stayboard.repository.InventoryItemRepository;
import az.aladdin.stayboard.repository.MenuItemRepository;
import az.aladdin.stayboard.repository.RecipeRepository;
import az.aladdin.stayboard.specification.RecipeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecipeService extends HotelAwareService {

    private final RecipeRepository recipeRepository;
    private final MenuItemRepository menuItemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final RecipeMapper recipeMapper;

    @Transactional
    public RecipeResponse create(CreateRecipeRequest request) {
        Long hotelId = getCurrentHotelId();
        MenuItemEntity menuItem = getMenuItemOrThrow(request.menuItemId(), hotelId);
        InventoryItemEntity inventoryItem = getInventoryItemOrThrow(request.inventoryItemId(), hotelId);

        if (recipeRepository.existsByMenuItem_IdAndInventoryItem_IdAndHotelId(
                menuItem.getId(), inventoryItem.getId(), hotelId)) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_RECIPE_ALREADY_EXISTS);
        }

        RecipeEntity entity = recipeMapper.toEntity(request, hotelId, menuItem, inventoryItem);
        return recipeMapper.toResponse(recipeRepository.save(entity));
    }

    @Transactional
    public RecipeResponse update(Long id, UpdateRecipeRequest request) {
        Long hotelId = getCurrentHotelId();
        RecipeEntity entity = getEntityOrThrow(id);
        MenuItemEntity menuItem = getMenuItemOrThrow(request.menuItemId(), hotelId);
        InventoryItemEntity inventoryItem = getInventoryItemOrThrow(request.inventoryItemId(), hotelId);
        recipeMapper.updateEntity(entity, request, menuItem, inventoryItem);
        return recipeMapper.toResponse(recipeRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public RecipeResponse get(Long id) {
        return recipeMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<RecipeResponse> search(RecipeSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        return recipeRepository.findAll(RecipeSpecification.withCriteria(hotelId, criteria), pageable)
                .map(recipeMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        recipeRepository.delete(getEntityOrThrow(id));
    }

    private RecipeEntity getEntityOrThrow(Long id) {
        Long hotelId = getCurrentHotelId();
        return recipeRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.RECIPE));
    }

    private MenuItemEntity getMenuItemOrThrow(Long menuItemId, Long hotelId) {
        return menuItemRepository.findByIdAndHotelId(menuItemId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.MENU_ITEM));
    }

    private InventoryItemEntity getInventoryItemOrThrow(Long inventoryItemId, Long hotelId) {
        return inventoryItemRepository.findByIdAndHotelId(inventoryItemId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.INVENTORY_ITEM));
    }
}
