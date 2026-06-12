package az.aladdin.stayboard.service.seating.support;

import az.aladdin.stayboard.entity.GuestInformation;
import az.aladdin.stayboard.entity.ReservationMainInfo;
import az.aladdin.stayboard.mapper.ReservationMainInfoMapper;
import az.aladdin.stayboard.model.request.ReservationMainInfoRequest;
import az.aladdin.stayboard.security.AuthenticatedUserSupport;
import az.aladdin.stayboard.security.GuestOrderAccess;
import az.aladdin.stayboard.security.GuestTableAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GuestReservationInfoResolver {

    private final ReservationMainInfoMapper reservationMainInfoMapper;

    public ReservationMainInfo resolveForCurrentUser(ReservationMainInfoRequest request) {
        ReservationMainInfo reservationMainInfo = reservationMainInfoMapper.toEntity(request);
        if (!AuthenticatedUserSupport.isGuest()) {
            return reservationMainInfo;
        }

        GuestInformation guestInformation = reservationMainInfo != null
                ? reservationMainInfo.guestInformation()
                : null;
        if (guestInformation == null) {
            guestInformation = new GuestInformation(null, null, null, null);
        }
        guestInformation = GuestTableAccess.attachGuestUserId(
                guestInformation,
                AuthenticatedUserSupport.requirePrincipal().getUserId()
        );
        if (guestInformation.guestEmail() == null) {
            guestInformation = new GuestInformation(
                    guestInformation.guestFirstName(),
                    guestInformation.guestLastName(),
                    GuestOrderAccess.comparableGuestEmail(AuthenticatedUserSupport.requirePrincipal().getEmail()),
                    guestInformation.guestUserId()
            );
        }

        if (reservationMainInfo == null) {
            return new ReservationMainInfo(null, null, null, guestInformation);
        }
        return new ReservationMainInfo(
                reservationMainInfo.reservationId(),
                reservationMainInfo.confirmationNumber(),
                reservationMainInfo.roomNumber(),
                guestInformation
        );
    }
}
