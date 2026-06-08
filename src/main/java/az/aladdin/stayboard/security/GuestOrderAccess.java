package az.aladdin.stayboard.security;

import az.aladdin.stayboard.entity.GuestInformation;
import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.model.enums.OrderStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class GuestOrderAccess {

    public static final int MODIFICATION_WINDOW_MINUTES = 5;

    public record Scope(Long guestUserId, String guestEmail) {
    }

    public static Optional<Scope> currentGuestScope() {
        if (!AuthenticatedUserSupport.isGuest()) {
            return Optional.empty();
        }
        JwtUserPrincipal principal = AuthenticatedUserSupport.requirePrincipal();
        return Optional.of(new Scope(principal.getUserId(), comparableGuestEmail(principal.getEmail())));
    }

    public static boolean ownsOrder(GuestInformation guestInformation, Scope scope) {
        if (guestInformation == null || scope == null) {
            return false;
        }
        if (scope.guestUserId() != null && scope.guestUserId().equals(guestInformation.guestUserId())) {
            return true;
        }
        return scope.guestEmail() != null
                && guestInformation.guestEmail() != null
                && scope.guestEmail().equalsIgnoreCase(guestInformation.guestEmail());
    }

    public static GuestInformation attachGuestUserId(GuestInformation guestInformation, Long guestUserId) {
        if (guestUserId == null) {
            return guestInformation;
        }
        if (guestInformation == null) {
            return new GuestInformation(null, null, null, guestUserId);
        }
        return new GuestInformation(
                guestInformation.guestFirstName(),
                guestInformation.guestLastName(),
                guestInformation.guestEmail(),
                guestUserId
        );
    }

    public static Predicate ownershipPredicate(
            CriteriaBuilder cb,
            Path<GuestInformation> guestInformationPath,
            Scope scope
    ) {
        List<Predicate> ownershipPredicates = new ArrayList<>();
        if (scope.guestUserId() != null) {
            ownershipPredicates.add(cb.equal(guestInformationPath.get("guestUserId"), scope.guestUserId()));
        }
        if (scope.guestEmail() != null) {
            ownershipPredicates.add(cb.equal(
                    cb.lower(guestInformationPath.get("guestEmail")),
                    scope.guestEmail()
            ));
        }
        if (ownershipPredicates.isEmpty()) {
            return cb.disjunction();
        }
        return cb.or(ownershipPredicates.toArray(Predicate[]::new));
    }

    public static String comparableGuestEmail(String email) {
        if (email == null || email.isBlank() || email.endsWith(".stayboard.internal")) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    public static void ensureGuestCanModify(OrderEntity order, LocalDateTime now) {
        if (!AuthenticatedUserSupport.isGuest()) {
            return;
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw ApiExceptions.forbidden(MessageKey.FORBIDDEN_GUEST_ORDER_MODIFICATION_NOT_ALLOWED);
        }
        if (order.getCreatedAt() == null) {
            throw ApiExceptions.forbidden(MessageKey.FORBIDDEN_GUEST_ORDER_MODIFICATION_WINDOW_EXPIRED);
        }
        LocalDateTime modificationDeadline = order.getCreatedAt().plusMinutes(MODIFICATION_WINDOW_MINUTES);
        if (now.isAfter(modificationDeadline)) {
            throw ApiExceptions.forbidden(MessageKey.FORBIDDEN_GUEST_ORDER_MODIFICATION_WINDOW_EXPIRED);
        }
    }

    public static void ensureGuestAllowedOrderStatusChange(OrderStatus requestedStatus, OrderStatus currentStatus) {
        if (!AuthenticatedUserSupport.isGuest() || requestedStatus == null) {
            return;
        }
        if (requestedStatus != currentStatus && requestedStatus != OrderStatus.CANCELLED) {
            throw ApiExceptions.forbidden(MessageKey.FORBIDDEN_GUEST_ORDER_MODIFICATION_NOT_ALLOWED);
        }
    }
}
