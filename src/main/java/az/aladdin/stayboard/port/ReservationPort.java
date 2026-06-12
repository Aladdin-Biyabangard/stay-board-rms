package az.aladdin.stayboard.port;

import az.aladdin.stayboard.model.response.ReservationDetailResponse;

public interface ReservationPort {

    ReservationDetailResponse getReservation(Long id);
}
