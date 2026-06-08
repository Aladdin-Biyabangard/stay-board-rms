package az.aladdin.stayboard.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationDetailResponse {

    private Long id;
    private String confirmationNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private ReservationGuestInfo guestInfo;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReservationGuestInfo {
        private String firstName;
        private String lastName;
    }
}
