package az.aladdin.stayboard.persistence;

import az.aladdin.stayboard.model.enums.SaleUnitType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SaleUnitTypeConverter implements AttributeConverter<SaleUnitType, String> {

    @Override
    public String convertToDatabaseColumn(SaleUnitType attribute) {
        return attribute == null ? SaleUnitType.PIECE.name() : attribute.name();
    }

    @Override
    public SaleUnitType convertToEntityAttribute(String dbData) {
        return SaleUnitType.fromValue(dbData);
    }
}
