package az.aladdin.stayboard.service.inventory;

import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.exception.NoteKey;
import az.aladdin.stayboard.exception.ReferenceType;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.entity.RecipeEntity;
import az.aladdin.stayboard.model.enums.InventoryTransactionType;
import az.aladdin.stayboard.repository.InventoryTransactionRepository;
import az.aladdin.stayboard.repository.RecipeRepository;
import az.aladdin.stayboard.util.OrderItemQuantitySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryConsumptionService extends HotelAwareService {

    private final RecipeRepository recipeRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryStockService inventoryStockService;

    @Transactional
    public void consumeForOrderItem(OrderItemEntity orderItem) {
        Long hotelId = getCurrentHotelId();
        if (inventoryTransactionRepository.existsByReferenceIdAndReferenceTypeAndTransactionType(
                orderItem.getId(),
                ReferenceType.ORDER_ITEM,
                InventoryTransactionType.CONSUMPTION
        )) {
            return;
        }
        if (orderItem.getMenuItem() == null) {
            return;
        }

        List<RecipeEntity> recipes = recipeRepository.findByMenuItemIdAndHotelId(
                orderItem.getMenuItem().getId(), hotelId
        );
        if (recipes.isEmpty()) {
            return;
        }

        BigDecimal servingQuantity = OrderItemQuantitySupport.effectiveServingQuantity(orderItem);
        for (RecipeEntity recipe : recipes) {
            BigDecimal consumption = recipe.getQuantityPerServing().multiply(servingQuantity).negate();
            inventoryStockService.applyDelta(
                    recipe.getInventoryItem().getId(),
                    consumption,
                    InventoryTransactionType.CONSUMPTION,
                    orderItem.getId(),
                    ReferenceType.ORDER_ITEM,
                    NoteKey.KITCHEN_CONSUMPTION
            );
        }
    }

    @Transactional
    public void reverseForOrderItem(Long orderItemId) {
        inventoryStockService.reverseConsumptionForOrderItem(orderItemId);
    }
}
