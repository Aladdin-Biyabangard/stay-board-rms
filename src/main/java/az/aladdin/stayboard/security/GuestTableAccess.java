package az.aladdin.stayboard.security;

import az.aladdin.stayboard.entity.GuestInformation;
import az.aladdin.stayboard.entity.ReservationMainInfo;
import az.aladdin.stayboard.entity.TableOccupancyEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.MessageKey;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class GuestTableAccess {

    public static Optional<GuestOrderAccess.Scope> currentGuestScope() {
        return GuestOrderAccess.currentGuestScope();
    }

    public static boolean ownsOccupancy(TableOccupancyEntity occupancy, GuestOrderAccess.Scope scope) {
        if (occupancy == null || scope == null || occupancy.getReservationMainInfo() == null) {
            return false;
        }
        return GuestOrderAccess.ownsOrder(occupancy.getReservationMainInfo().guestInformation(), scope);
    }

    public static Predicate ownershipPredicate(
            CriteriaBuilder cb,
            Path<ReservationMainInfo> reservationMainInfoPath,
            GuestOrderAccess.Scope scope
    ) {
        return GuestOrderAccess.ownershipPredicate(
                cb,
                reservationMainInfoPath.get("guestInformation"),
                scope
        );
    }

    public static void ensureGuestCanReserve() {
        if (!AuthenticatedUserSupport.isGuest()) {
            return;
        }
        // Guest reservations are always allowed when authenticated.
    }

    public static void ensureGuestCanCancel(TableOccupancyEntity occupancy) {
        if (!AuthenticatedUserSupport.isGuest()) {
            return;
        }
        GuestOrderAccess.Scope scope = currentGuestScope()
                .orElseThrow(() -> ApiExceptions.forbidden(MessageKey.FORBIDDEN_GUEST_TABLE_RESERVATION_NOT_ALLOWED));
        if (!ownsOccupancy(occupancy, scope)) {
            throw ApiExceptions.notFound(az.aladdin.stayboard.exception.EntityKey.TABLE_OCCUPANCY);
        }
    }

    public static GuestInformation attachGuestUserId(GuestInformation guestInformation, Long guestUserId) {
        return GuestOrderAccess.attachGuestUserId(guestInformation, guestUserId);
    }
}
