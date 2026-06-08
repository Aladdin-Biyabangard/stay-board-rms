package az.aladdin.stayboard.model.request;

public record ReservationMainInfoRequest(
        Long reservationId,
        String confirmationNumber,
        String roomNumber,
        GuestInformationRequest guestInformation
) {
}
