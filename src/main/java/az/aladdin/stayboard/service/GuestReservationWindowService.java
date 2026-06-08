package az.aladdin.stayboard.service;

import az.aladdin.stayboard.client.StayBoardGuestStayContextClient;
import az.aladdin.stayboard.client.StayBoardReservationClient;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.model.response.GuestStayContextResponse;
import az.aladdin.stayboard.model.response.ReservationDetailResponse;
import az.aladdin.stayboard.security.AuthenticatedUserSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestReservationWindowService {

    public static final int MAX_DAYS_AFTER_CHECKOUT = 1;

    private final StayBoardReservationClient stayBoardReservationClient;
    private final StayBoardGuestStayContextClient stayBoardGuestStayContextClient;

    public void ensureWithinStayWindow(Long reservationId, LocalDateTime startHotelLocal, LocalDateTime endHotelLocal) {
        if (!AuthenticatedUserSupport.isGuest() || startHotelLocal == null || endHotelLocal == null) {
            return;
        }

        LocalDate maxReservationDate = resolveMaxReservationDate(reservationId);
        if (maxReservationDate == null) {
            return;
        }

        if (startHotelLocal.toLocalDate().isAfter(maxReservationDate)
                || endHotelLocal.toLocalDate().isAfter(maxReservationDate)) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_TABLE_RESERVATION_BEYOND_STAY, maxReservationDate);
        }
    }

    public LocalDate resolveMaxReservationDate(Long reservationId) {
        Long effectiveReservationId = reservationId != null
                ? reservationId
                : resolveGuestReservationId();
        if (effectiveReservationId == null) {
            return null;
        }

        LocalDate checkOutDate = loadCheckOutDate(effectiveReservationId);
        if (checkOutDate == null) {
            return null;
        }
        return checkOutDate.plusDays(MAX_DAYS_AFTER_CHECKOUT);
    }

    private Long resolveGuestReservationId() {
        if (!AuthenticatedUserSupport.isGuest()) {
            return null;
        }
        try {
            GuestStayContextResponse stayContext = stayBoardGuestStayContextClient.getStayContext();
            return stayContext != null ? stayContext.getReservationId() : null;
        } catch (Exception ex) {
            log.warn("Failed to resolve guest stay context for reservation window validation", ex);
            return null;
        }
    }

    private LocalDate loadCheckOutDate(Long reservationId) {
        try {
            ReservationDetailResponse reservation = stayBoardReservationClient.getReservation(reservationId);
            return reservation != null ? reservation.getCheckOutDate() : null;
        } catch (Exception ex) {
            log.warn("Failed to load checkout date for reservation {}", reservationId, ex);
            return null;
        }
    }
}
