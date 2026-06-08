package az.aladdin.stayboard.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record ReservationMainInfo(
        Long reservationId,
        String confirmationNumber,
        String roomNumber,
        GuestInformation guestInformation
) {
}
