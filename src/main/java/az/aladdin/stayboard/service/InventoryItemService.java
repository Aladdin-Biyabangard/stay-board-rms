package az.aladdin.stayboard.service;

import az.aladdin.stayboard.context.HotelContextHolder;
import az.aladdin.stayboard.entity.InventoryItemEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.exception.ReferenceType;
import az.aladdin.stayboard.mapper.InventoryItemMapper;
import az.aladdin.stayboard.model.enums.InventoryTransactionType;
import az.aladdin.stayboard.model.request.AdjustInventoryStockRequest;
import az.aladdin.stayboard.model.request.CreateInventoryItemRequest;
import az.aladdin.stayboard.model.request.PatchInventoryItemRequest;
import az.aladdin.stayboard.model.request.UpdateInventoryItemRequest;
import az.aladdin.stayboard.model.request.search.InventoryItemSearchCriteria;
import az.aladdin.stayboard.model.response.InventoryItemResponse;
import az.aladdin.stayboard.repository.InventoryItemRepository;
import az.aladdin.stayboard.repository.RecipeRepository;
import az.aladdin.stayboard.specification.InventoryItemSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Service
@RequiredArgsConstructor
public class InventoryItemService {

    private static final EnumSet<InventoryTransactionType> MANUAL_TYPES = EnumSet.of(
            InventoryTransactionType.RECEIPT,
            InventoryTransactionType.ADJUSTMENT,
            InventoryTransactionType.WASTE
    );

    private final InventoryItemRepository inventoryItemRepository;
    private final RecipeRepository recipeRepository;
    private final InventoryItemMapper inventoryItemMapper;
    private final InventoryStockService inventoryStockService;

    @Transactional
    public InventoryItemResponse create(CreateInventoryItemRequest request) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        InventoryItemEntity entity = inventoryItemMapper.toEntity(request, hotelId);
        return inventoryItemMapper.toResponse(inventoryItemRepository.save(entity));
    }

    @Transactional
    public InventoryItemResponse update(Long id, UpdateInventoryItemRequest request) {
        InventoryItemEntity entity = getEntityOrThrow(id);
        inventoryItemMapper.updateEntity(entity, request);
        return inventoryItemMapper.toResponse(inventoryItemRepository.save(entity));
    }

    @Transactional
    public InventoryItemResponse patch(Long id, PatchInventoryItemRequest request) {
        InventoryItemEntity entity = getEntityOrThrow(id);
        inventoryItemMapper.patchEntity(entity, request);
        return inventoryItemMapper.toResponse(inventoryItemRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse get(Long id) {
        return inventoryItemMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> search(InventoryItemSearchCriteria criteria, Pageable pageable) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        return inventoryItemRepository.findAll(InventoryItemSpecification.withCriteria(hotelId, criteria), pageable)
                .map(inventoryItemMapper::toResponse);
    }

    @Transactional
    public InventoryItemResponse adjustStock(Long id, AdjustInventoryStockRequest request) {
        if (!MANUAL_TYPES.contains(request.transactionType())) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_STOCK_TRANSACTION_TYPE);
        }
        InventoryItemEntity item = inventoryStockService.applyDelta(
                id,
                request.quantityDelta(),
                request.transactionType(),
                null,
                ReferenceType.MANUAL,
                request.notes()
        );
        return inventoryItemMapper.toResponse(item);
    }

    @Transactional
    public void delete(Long id) {
        InventoryItemEntity entity = getEntityOrThrow(id);
        if (recipeRepository.existsByInventoryItem_IdAndHotelId(entity.getId(), entity.getHotelId())) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_INVENTORY_ITEM_HAS_RECIPES);
        }
        inventoryItemRepository.delete(entity);
    }

    private InventoryItemEntity getEntityOrThrow(Long id) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        return inventoryItemRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.INVENTORY_ITEM));
    }
}
