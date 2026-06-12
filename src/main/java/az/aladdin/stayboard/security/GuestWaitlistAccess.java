package az.aladdin.stayboard.security;

import az.aladdin.stayboard.entity.GuestInformation;
import az.aladdin.stayboard.entity.ReservationMainInfo;
import az.aladdin.stayboard.entity.WaitlistEntryEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class GuestWaitlistAccess {

    public static Optional<GuestOrderAccess.Scope> currentGuestScope() {
        return GuestOrderAccess.currentGuestScope();
    }

    public static boolean ownsEntry(WaitlistEntryEntity entry, GuestOrderAccess.Scope scope) {
        if (entry == null || scope == null || entry.getReservationMainInfo() == null) {
            return false;
        }
        return GuestOrderAccess.ownsOrder(entry.getReservationMainInfo().guestInformation(), scope);
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

    public static void ensureGuestCanCancel(WaitlistEntryEntity entry) {
        if (!AuthenticatedUserSupport.isGuest()) {
            return;
        }
        GuestOrderAccess.Scope scope = currentGuestScope()
                .orElseThrow(() -> ApiExceptions.forbidden(MessageKey.FORBIDDEN_GUEST_WAITLIST_NOT_ALLOWED));
        if (!ownsEntry(entry, scope)) {
            throw ApiExceptions.notFound(EntityKey.WAITLIST_ENTRY);
        }
    }
}
