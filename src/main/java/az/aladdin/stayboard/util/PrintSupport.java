package az.aladdin.stayboard.util;

import az.aladdin.stayboard.entity.GuestInformation;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.model.enums.SaleUnitType;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class PrintSupport {

    public static final String DEFAULT_CURRENCY_CODE = "AZN";

    public static String formatGuestName(GuestInformation guestInformation) {
        if (guestInformation == null) {
            return null;
        }
        String firstName = guestInformation.guestFirstName();
        String lastName = guestInformation.guestLastName();
        if (firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank()) {
            return firstName.trim() + " " + lastName.trim();
        }
        if (firstName != null && !firstName.isBlank()) {
            return firstName.trim();
        }
        if (lastName != null && !lastName.isBlank()) {
            return lastName.trim();
        }
        return guestInformation.guestEmail();
    }

    public static String formatQuantityLabel(OrderItemEntity item) {
        SaleUnitType saleUnitType = item.getMenuItem() != null && item.getMenuItem().getSaleUnitType() != null
                ? item.getMenuItem().getSaleUnitType()
                : SaleUnitType.PIECE;
        if (saleUnitType.isWeightBased()) {
            BigDecimal weight = item.getWeightQuantity();
            if (weight == null) {
                return "—";
            }
            return weight.stripTrailingZeros().toPlainString() + " kg";
        }
        return String.valueOf(item.getQuantity());
    }

    public static String resolveServiceLocation(String tableNumber, String roomNumber) {
        if (tableNumber != null && !tableNumber.isBlank()) {
            return "Table " + tableNumber.trim();
        }
        if (roomNumber != null && !roomNumber.isBlank()) {
            return "Room " + roomNumber.trim();
        }
        return "—";
    }

    public static String currencySymbol(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return "₼";
        }
        return switch (currencyCode.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "AZN" -> "₼";
            default -> currencyCode + " ";
        };
    }

    public static String formatMoney(BigDecimal amount, String currencyCode) {
        BigDecimal normalized = amount != null ? amount : BigDecimal.ZERO;
        return currencySymbol(currencyCode) + normalized.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
