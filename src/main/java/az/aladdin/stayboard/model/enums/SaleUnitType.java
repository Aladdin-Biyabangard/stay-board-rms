package az.aladdin.stayboard.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SaleUnitType {
    PIECE,
    PORTION,
    WEIGHT;

    @JsonCreator
    public static SaleUnitType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return PIECE;
        }
        if ("COUNT".equalsIgnoreCase(value)) {
            return PIECE;
        }
        return valueOf(value.toUpperCase());
    }

    public boolean isWeightBased() {
        return this == WEIGHT;
    }
}
