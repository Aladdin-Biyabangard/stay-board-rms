package az.aladdin.stayboard.model.response;

public record GuestInformationResponse(
        String guestFirstName,
        String guestLastName,
        String guestEmail
) {
}
