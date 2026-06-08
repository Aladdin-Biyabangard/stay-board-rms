package az.aladdin.stayboard.model.response;

public record ReservationMainInfoResponse(
        Long reservationId,
        String confirmationNumber,
        String roomNumber,
        GuestInformationResponse guestInformation
) {
}
