package az.aladdin.stayboard.model.request;

public record GuestInformationRequest(
        String guestFirstName,
        String guestLastName,
        String guestEmail
) {
}
