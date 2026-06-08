package az.aladdin.stayboard.model.response.folio;

import az.aladdin.stayboard.model.enums.FolioChargeType;
import az.aladdin.stayboard.model.enums.TaxType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FolioChargeResponse {

    private Long id;
    private FolioChargeType chargeType;
    private String chargeName;
    private String description;
    private BigDecimal netAmount;
    private BigDecimal taxAmount;
    private BigDecimal grossAmount;
    private int quantity;
    private BigDecimal taxRate;
    private TaxType taxType;
    private LocalDate chargeDate;
    private Boolean isVoided;
    private LocalDateTime voidedAt;
    private String voidedBy;
    private String voidReason;
    private LocalDateTime createdAt;
    private String createdBy;
}
