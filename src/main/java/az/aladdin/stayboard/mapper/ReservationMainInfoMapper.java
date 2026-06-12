package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.GuestInformation;
import az.aladdin.stayboard.entity.ReservationMainInfo;
import az.aladdin.stayboard.model.request.GuestInformationRequest;
import az.aladdin.stayboard.model.request.ReservationMainInfoRequest;
import az.aladdin.stayboard.model.response.GuestInformationResponse;
import az.aladdin.stayboard.model.response.ReservationMainInfoResponse;
import org.springframework.stereotype.Component;

@Component
public class ReservationMainInfoMapper {

    public ReservationMainInfo toEntity(ReservationMainInfoRequest request) {
        if (request == null) {
            return null;
        }
        return new ReservationMainInfo(
                request.reservationId(),
                request.confirmationNumber(),
                request.roomNumber(),
                toGuestInformation(request.guestInformation())
        );
    }

    public ReservationMainInfoResponse toResponse(ReservationMainInfo reservationMainInfo, boolean maskGuestDetails) {
        if (reservationMainInfo == null) {
            return null;
        }
        GuestInformationResponse guestInformation = maskGuestDetails
                ? null
                : toGuestInformationResponse(reservationMainInfo.guestInformation());
        return new ReservationMainInfoResponse(
                reservationMainInfo.reservationId(),
                reservationMainInfo.confirmationNumber(),
                reservationMainInfo.roomNumber(),
                guestInformation
        );
    }

    private GuestInformation toGuestInformation(GuestInformationRequest request) {
        if (request == null) {
            return null;
        }
        return new GuestInformation(
                request.guestFirstName(),
                request.guestLastName(),
                request.guestEmail(),
                null
        );
    }

    private GuestInformationResponse toGuestInformationResponse(GuestInformation guestInformation) {
        if (guestInformation == null) {
            return null;
        }
        return new GuestInformationResponse(
                guestInformation.guestFirstName(),
                guestInformation.guestLastName(),
                guestInformation.guestEmail()
        );
    }
}
