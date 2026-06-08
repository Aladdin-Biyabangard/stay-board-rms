package az.aladdin.stayboard.config.security;

import az.aladdin.stayboard.security.JwtUserPrincipal;
import az.aladdin.stayboard.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = jwtUtil.resolveToken(request);
        if (jwtToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null && jwtUtil.isAccessTokenValid(jwtToken)) {
                String email = jwtUtil.extractEmail(jwtToken);
                Long userId = jwtUtil.extractUserId(jwtToken);
                Long hotelId = jwtUtil.extractHotelId(jwtToken);
                List<SimpleGrantedAuthority> authorities = jwtUtil.extractAuthorities(jwtToken).stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                JwtUserPrincipal principal = JwtUserPrincipal.builder()
                        .email(email)
                        .userId(userId)
                        .hotelId(hotelId)
                        .authorities(authorities)
                        .build();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                MDC.put("userId", String.valueOf(userId));
                log.debug("Authenticated RMS request for user: {}", email);
            }
        } catch (Exception e) {
            log.warn("Failed to authenticate JWT for RMS request: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
