package az.aladdin.stayboard.model.request.folio;

import az.aladdin.stayboard.model.enums.FolioChargeType;
import az.aladdin.stayboard.model.enums.TaxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddFolioChargeRequest {

    private String chargeName;
    private String description;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal taxRate;
    private TaxType taxType;
    private FolioChargeType chargeType;
}
