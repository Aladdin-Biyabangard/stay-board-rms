package az.aladdin.stayboard.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GuestStayContextResponse {

    private Long reservationId;
    private String confirmationNumber;
    private String roomNumber;
}
