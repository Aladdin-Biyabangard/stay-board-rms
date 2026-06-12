package az.aladdin.stayboard.util;

import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.entity.OrderItemModifierEntity;
import az.aladdin.stayboard.model.enums.SaleUnitType;
import az.aladdin.stayboard.model.enums.TaxType;
import az.aladdin.stayboard.model.pricing.TaxResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class OrderItemPricingSupport {

    private static final int MONEY_SCALE = 2;
    private static final int WEIGHT_SCALE = 3;
    private static final TaxType DEFAULT_MENU_TAX_TYPE = TaxType.INCLUDE;

    private OrderItemPricingSupport() {
    }

    public record OrderItemAmounts(
            BigDecimal netAmount,
            BigDecimal taxAmount,
            BigDecimal grossAmount,
            BigDecimal taxRate,
            TaxType taxType
    ) {
    }

    public record FolioChargePosting(
            BigDecimal unitPrice,
            int quantity,
            BigDecimal taxRate,
            TaxType taxType
    ) {
    }

    public static void applyPricing(OrderItemEntity entity, MenuItemEntity menuItem) {
        applyPricing(entity, menuItem, BigDecimal.ZERO);
    }

    public static void applyPricing(OrderItemEntity entity, MenuItemEntity menuItem, BigDecimal modifierPriceDelta) {
        if (menuItem.getSaleUnitType().isWeightBased()) {
            entity.setQuantity(1L);
            entity.setWeightQuantity(normalizeWeight(entity.getWeightQuantity()));
        }
        OrderItemAmounts amounts = calculate(menuItem, entity.getQuantity(), entity.getWeightQuantity(), modifierPriceDelta);
        entity.setNetAmount(amounts.netAmount());
        entity.setTaxAmount(amounts.taxAmount());
        entity.setGrossAmount(amounts.grossAmount());
        entity.setTaxRate(amounts.taxRate());
        entity.setTaxType(amounts.taxType());
    }

    public static OrderItemAmounts calculate(MenuItemEntity menuItem, long quantity, BigDecimal weightQuantity) {
        return calculate(menuItem, quantity, weightQuantity, BigDecimal.ZERO);
    }

    public static OrderItemAmounts calculate(
            MenuItemEntity menuItem,
            long quantity,
            BigDecimal weightQuantity,
            BigDecimal modifierPriceDelta
    ) {
        BigDecimal basePrice = menuItem.getPrice() != null ? menuItem.getPrice() : BigDecimal.ZERO;
        BigDecimal modifierDelta = modifierPriceDelta != null ? modifierPriceDelta : BigDecimal.ZERO;
        BigDecimal unitPrice = basePrice.add(modifierDelta);
        BigDecimal multiplier = lineMultiplier(menuItem.getSaleUnitType(), quantity, weightQuantity);
        BigDecimal lineAmount = unitPrice.multiply(multiplier).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal taxRate = menuItem.getTaxRate() != null ? menuItem.getTaxRate() : BigDecimal.ZERO;
        TaxType taxType = menuItem.getTaxType() != null ? menuItem.getTaxType() : DEFAULT_MENU_TAX_TYPE;
        TaxResult taxResult = TaxCalculator.calculateTax(lineAmount, taxRate, taxType);
        return new OrderItemAmounts(
                taxResult.netAmount(),
                taxResult.taxAmount(),
                taxResult.grossAmount(),
                taxRate,
                taxType
        );
    }

    public static BigDecimal sumModifierPriceDeltas(OrderItemEntity orderItem) {
        if (orderItem.getModifiers() == null || orderItem.getModifiers().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return orderItem.getModifiers().stream()
                .map(OrderItemModifierEntity::getPriceDelta)
                .map(delta -> delta != null ? delta : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static FolioChargePosting buildFolioPosting(OrderItemEntity orderItem) {
        MenuItemEntity menuItem = orderItem.getMenuItem();
        if (menuItem == null) {
            return buildFolioPostingFromStoredAmounts(orderItem);
        }

        BigDecimal modifierPriceDelta = sumModifierPriceDeltas(orderItem);
        OrderItemAmounts amounts = calculate(
                menuItem,
                orderItem.getQuantity(),
                orderItem.getWeightQuantity(),
                modifierPriceDelta
        );
        SaleUnitType saleUnitType = menuItem.getSaleUnitType() != null ? menuItem.getSaleUnitType() : SaleUnitType.PIECE;

        if (saleUnitType.isWeightBased()) {
            BigDecimal lineAmount = amounts.taxType() == TaxType.INCLUDE
                    ? amounts.grossAmount()
                    : amounts.netAmount();
            return new FolioChargePosting(lineAmount, 1, amounts.taxRate(), amounts.taxType());
        }

        BigDecimal catalogUnitPrice = menuItem.getPrice() != null ? menuItem.getPrice() : BigDecimal.ZERO;
        BigDecimal unitPrice = catalogUnitPrice.add(modifierPriceDelta);
        int postingQuantity = Math.toIntExact(orderItem.getQuantity());
        return new FolioChargePosting(unitPrice, postingQuantity, amounts.taxRate(), amounts.taxType());
    }

    private static FolioChargePosting buildFolioPostingFromStoredAmounts(OrderItemEntity orderItem) {
        TaxType taxType = orderItem.getTaxType() != null ? orderItem.getTaxType() : DEFAULT_MENU_TAX_TYPE;
        BigDecimal taxRate = orderItem.getTaxRate() != null ? orderItem.getTaxRate() : BigDecimal.ZERO;
        BigDecimal lineAmount = taxType == TaxType.INCLUDE
                ? nonNullAmount(orderItem.getGrossAmount())
                : nonNullAmount(orderItem.getNetAmount());
        return new FolioChargePosting(lineAmount, 1, taxRate, taxType);
    }

    public static BigDecimal lineMultiplier(SaleUnitType saleUnitType, long quantity, BigDecimal weightQuantity) {
        if (saleUnitType != null && saleUnitType.isWeightBased()) {
            BigDecimal normalizedWeight = normalizeWeight(weightQuantity);
            return normalizedWeight != null ? normalizedWeight : BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(quantity);
    }

    public static BigDecimal normalizeWeight(BigDecimal weightQuantity) {
        if (weightQuantity == null) {
            return null;
        }
        return weightQuantity.setScale(WEIGHT_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal nonNullAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }
}
