package az.aladdin.stayboard.service;

import az.aladdin.stayboard.context.HotelContextHolder;
import az.aladdin.stayboard.entity.InventoryItemEntity;
import az.aladdin.stayboard.entity.InventoryTransactionEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.exception.NoteKey;
import az.aladdin.stayboard.exception.ReferenceType;
import az.aladdin.stayboard.model.enums.InventoryTransactionType;
import az.aladdin.stayboard.model.enums.InventoryUnitType;
import az.aladdin.stayboard.repository.InventoryItemRepository;
import az.aladdin.stayboard.repository.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryStockService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Transactional
    public InventoryItemEntity applyDelta(
            Long inventoryItemId,
            BigDecimal quantityDelta,
            InventoryTransactionType transactionType,
            Long referenceId,
            String referenceType,
            String notes
    ) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        InventoryItemEntity item = inventoryItemRepository.findByIdAndHotelId(inventoryItemId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.INVENTORY_ITEM));

        validateQuantity(item, quantityDelta);

        BigDecimal newStock = item.getCurrentStock().add(quantityDelta);
        if (newStock.signum() < 0) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_INSUFFICIENT_STOCK, item.getName());
        }

        item.setCurrentStock(normalizeStock(item, newStock));
        inventoryItemRepository.save(item);

        inventoryTransactionRepository.save(InventoryTransactionEntity.builder()
                .hotelId(hotelId)
                .inventoryItem(item)
                .quantityDelta(quantityDelta)
                .transactionType(transactionType)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .notes(notes)
                .build());

        return item;
    }

    @Transactional
    public void reverseConsumptionForOrderItem(Long orderItemId) {
        List<InventoryTransactionEntity> consumptions = inventoryTransactionRepository
                .findByReferenceIdAndReferenceTypeAndTransactionType(
                        orderItemId, ReferenceType.ORDER_ITEM, InventoryTransactionType.CONSUMPTION
                );
        if (consumptions.isEmpty()) {
            return;
        }
        boolean alreadyReversed = inventoryTransactionRepository.existsByReferenceIdAndReferenceTypeAndTransactionType(
                orderItemId, ReferenceType.ORDER_ITEM, InventoryTransactionType.REVERSAL
        );
        if (alreadyReversed) {
            return;
        }

        for (InventoryTransactionEntity consumption : consumptions) {
            applyDelta(
                    consumption.getInventoryItem().getId(),
                    consumption.getQuantityDelta().negate(),
                    InventoryTransactionType.REVERSAL,
                    orderItemId,
                    ReferenceType.ORDER_ITEM,
                    NoteKey.ORDER_ITEM_REVERSAL
            );
        }
    }

    private void validateQuantity(InventoryItemEntity item, BigDecimal quantityDelta) {
        if (quantityDelta == null || quantityDelta.signum() == 0) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_STOCK_QUANTITY);
        }
        if (item.getUnitType() == InventoryUnitType.COUNT) {
            if (quantityDelta.stripTrailingZeros().scale() > 0) {
                throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_COUNT_MUST_BE_WHOLE);
            }
        }
    }

    private BigDecimal normalizeStock(InventoryItemEntity item, BigDecimal stock) {
        if (item.getUnitType() == InventoryUnitType.COUNT) {
            return stock.setScale(0, RoundingMode.UNNECESSARY);
        }
        return stock.setScale(4, RoundingMode.HALF_UP);
    }
}
