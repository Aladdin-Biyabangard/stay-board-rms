package az.aladdin.stayboard.security;

import az.aladdin.stayboard.model.enums.Role;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@UtilityClass
public class AuthenticatedUserSupport {

    public static Optional<JwtUserPrincipal> currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static JwtUserPrincipal requirePrincipal() {
        return currentPrincipal()
                .orElseThrow(() -> new IllegalStateException("Authenticated user is required"));
    }

    public static boolean hasRole(Role role) {
        String authority = "ROLE_" + role.name();
        return currentPrincipal()
                .map(principal -> principal.getAuthorities().stream()
                        .anyMatch(granted -> authority.equals(granted.getAuthority())))
                .orElse(false);
    }

    public static boolean isGuest() {
        return hasRole(Role.GUEST);
    }
}
