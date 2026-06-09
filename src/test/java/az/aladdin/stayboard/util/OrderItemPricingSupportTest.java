package az.aladdin.stayboard.util;

import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.model.enums.SaleUnitType;
import az.aladdin.stayboard.model.enums.TaxType;
import az.aladdin.stayboard.util.OrderItemPricingSupport.FolioChargePosting;
import az.aladdin.stayboard.util.OrderItemPricingSupport.OrderItemAmounts;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderItemPricingSupportTest {

    @Test
    void calculatePieceExcludeTax() {
        MenuItemEntity menuItem = menuItem(SaleUnitType.PIECE, "10.00", "18", TaxType.EXCLUDE);

        OrderItemAmounts amounts = OrderItemPricingSupport.calculate(menuItem, 2, null);

        assertEquals(new BigDecimal("20.00"), amounts.netAmount());
        assertEquals(new BigDecimal("3.60"), amounts.taxAmount());
        assertEquals(new BigDecimal("23.60"), amounts.grossAmount());
        assertEquals(TaxType.EXCLUDE, amounts.taxType());
    }

    @Test
    void calculatePortionIncludeTax() {
        MenuItemEntity menuItem = menuItem(SaleUnitType.PORTION, "11.80", "18", TaxType.INCLUDE);

        OrderItemAmounts amounts = OrderItemPricingSupport.calculate(menuItem, 2, null);

        assertEquals(new BigDecimal("20.00"), amounts.netAmount());
        assertEquals(new BigDecimal("3.60"), amounts.taxAmount());
        assertEquals(new BigDecimal("23.60"), amounts.grossAmount());
    }

    @Test
    void calculateWeightExcludeTax() {
        MenuItemEntity menuItem = menuItem(SaleUnitType.WEIGHT, "20.00", "18", TaxType.EXCLUDE);

        OrderItemAmounts amounts = OrderItemPricingSupport.calculate(menuItem, 1, new BigDecimal("0.250"));

        assertEquals(new BigDecimal("5.00"), amounts.netAmount());
        assertEquals(new BigDecimal("0.90"), amounts.taxAmount());
        assertEquals(new BigDecimal("5.90"), amounts.grossAmount());
    }

    @Test
    void normalizeWeightRoundsToThreeDecimals() {
        assertEquals(new BigDecimal("0.251"), OrderItemPricingSupport.normalizeWeight(new BigDecimal("0.2505")));
    }

    @Test
    void folioPostingUsesGrossUnitPriceForIncludeTax() {
        MenuItemEntity menuItem = menuItem(SaleUnitType.PIECE, "11.80", "18", TaxType.INCLUDE);
        OrderItemEntity orderItem = orderItem(menuItem, 2);

        OrderItemPricingSupport.applyPricing(orderItem, menuItem);
        FolioChargePosting posting = OrderItemPricingSupport.buildFolioPosting(orderItem);

        assertEquals(new BigDecimal("11.80"), posting.unitPrice());
        assertEquals(2, posting.quantity());
        assertEquals(TaxType.INCLUDE, posting.taxType());
    }

    @Test
    void folioPostingUsesNetUnitPriceForExcludeTax() {
        MenuItemEntity menuItem = menuItem(SaleUnitType.PIECE, "10.00", "18", TaxType.EXCLUDE);
        OrderItemEntity orderItem = orderItem(menuItem, 2);

        OrderItemPricingSupport.applyPricing(orderItem, menuItem);
        FolioChargePosting posting = OrderItemPricingSupport.buildFolioPosting(orderItem);

        assertEquals(new BigDecimal("10.00"), posting.unitPrice());
        assertEquals(2, posting.quantity());
        assertEquals(TaxType.EXCLUDE, posting.taxType());
    }

    @Test
    void folioPostingUsesCatalogUnitPriceForPieceItems() {
        MenuItemEntity menuItem = menuItem(SaleUnitType.PIECE, "3.33", "18", TaxType.INCLUDE);
        OrderItemEntity orderItem = orderItem(menuItem, 3);

        OrderItemPricingSupport.applyPricing(orderItem, menuItem);
        FolioChargePosting posting = OrderItemPricingSupport.buildFolioPosting(orderItem);

        assertEquals(new BigDecimal("3.33"), posting.unitPrice());
        assertEquals(3, posting.quantity());
        assertEquals(TaxType.INCLUDE, posting.taxType());
    }

    @Test
    void calculateDefaultsToIncludeTaxWhenMenuTaxTypeMissing() {
        MenuItemEntity menuItem = menuItem(SaleUnitType.PIECE, "11.80", "18", null);

        OrderItemAmounts amounts = OrderItemPricingSupport.calculate(menuItem, 1, null);

        assertEquals(new BigDecimal("10.00"), amounts.netAmount());
        assertEquals(new BigDecimal("1.80"), amounts.taxAmount());
        assertEquals(new BigDecimal("11.80"), amounts.grossAmount());
        assertEquals(TaxType.INCLUDE, amounts.taxType());
    }

    private static MenuItemEntity menuItem(SaleUnitType saleUnitType, String price, String taxRate, TaxType taxType) {
        MenuItemEntity menuItem = new MenuItemEntity();
        menuItem.setSaleUnitType(saleUnitType);
        menuItem.setPrice(new BigDecimal(price));
        menuItem.setTaxRate(new BigDecimal(taxRate));
        menuItem.setTaxType(taxType);
        return menuItem;
    }

    private static OrderItemEntity orderItem(MenuItemEntity menuItem, long quantity) {
        OrderEntity order = new OrderEntity();
        order.setOrderNumber("RMS-1");
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrder(order);
        orderItem.setMenuItem(menuItem);
        orderItem.setQuantity(quantity);
        return orderItem;
    }
}
