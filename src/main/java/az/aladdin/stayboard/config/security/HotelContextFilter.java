package az.aladdin.stayboard.config.security;

import az.aladdin.stayboard.context.HotelContextHolder;
import az.aladdin.stayboard.model.enums.Role;
import az.aladdin.stayboard.security.JwtUserPrincipal;
import az.aladdin.stayboard.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotelContextFilter extends OncePerRequestFilter {

    private static final String DIRECTOR_AUTHORITY = "ROLE_" + Role.DIRECTOR.name();

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof JwtUserPrincipal principal) {

                Long hotelId = principal.getHotelId();
                String jwtToken = jwtUtil.resolveToken(request);
                if (hotelId == null && jwtToken != null) {
                    hotelId = jwtUtil.extractHotelId(jwtToken);
                }

                boolean isDirector = principal.getAuthorities().stream()
                        .anyMatch(authority -> DIRECTOR_AUTHORITY.equals(authority.getAuthority()));

                HotelContextHolder.setHotelId(hotelId);
                HotelContextHolder.setIsDirector(isDirector);
                log.debug("RMS hotel context set - hotelId: {}, director: {}", hotelId, isDirector);
            }

            filterChain.doFilter(request, response);
        } finally {
            HotelContextHolder.clear();
        }
    }
}
