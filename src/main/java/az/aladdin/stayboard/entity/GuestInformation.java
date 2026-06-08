package az.aladdin.stayboard.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record GuestInformation(
        String guestFirstName,
        String guestLastName,
        String guestEmail,
        Long guestUserId
) {
}
