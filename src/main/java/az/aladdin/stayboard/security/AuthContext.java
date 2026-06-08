package az.aladdin.stayboard.security;

import az.aladdin.stayboard.context.HotelContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthContext {

    public Optional<JwtUserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return Optional.of(jwtUserPrincipal);
        }
        return Optional.empty();
    }

    public Long getCurrentHotelId() {
        return HotelContextHolder.getHotelId();
    }

    public Long getCurrentHotelIdOrThrow() {
        return HotelContextHolder.getHotelIdOrThrow();
    }

    public boolean isDirector() {
        return HotelContextHolder.isDirector();
    }
}
