package az.aladdin.stayboard.model.response;

import az.aladdin.stayboard.model.enums.WaitlistStatus;

import java.time.LocalDateTime;

public record WaitlistEntryResponse(
        Long id,
        Long hotelId,
        Integer partySize,
        WaitlistStatus status,
        ReservationMainInfoResponse reservationMainInfo,
        String notes,
        Integer estimatedWaitMinutes,
        Long preferredTableId,
        String preferredTableNumber,
        Long seatedTableId,
        String seatedTableNumber,
        int queuePosition,
        LocalDateTime createdAt,
        String createdBy,
        boolean ownedByCurrentGuest
) {
}
