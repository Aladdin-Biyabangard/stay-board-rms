package az.aladdin.stayboard.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";

    private final Key signingKey;

    public JwtUtil(@Value("${security.jwtProperties.secret}") String secretKey) {
        this.signingKey = buildSigningKey(secretKey);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!ACCESS_TOKEN_TYPE.equals(extractTokenType(claims))) {
                return false;
            }
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("Access token expired for subject: {}", e.getClaims().getSubject());
            return false;
        } catch (Exception e) {
            log.debug("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractTokenType(parseClaims(token));
    }

    public Long extractHotelId(String token) {
        return extractLongClaim(token, "hotel_id");
    }

    public Long extractUserId(String token) {
        return extractLongClaim(token, "user_id");
    }

    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        Claims claims = parseClaims(token);
        Object authorities = claims.get("authorities");
        if (authorities instanceof List<?> authorityList) {
            return authorityList.stream()
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }

    private String extractTokenType(Claims claims) {
        Object tokenType = claims.get(TOKEN_TYPE_CLAIM);
        return tokenType != null ? tokenType.toString() : null;
    }

    private Long extractLongClaim(String token, String claimName) {
        Claims claims = parseClaims(token);
        Object value = claims.get(claimName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        return claimResolver.apply(parseClaims(token));
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private static Key buildSigningKey(String secretKey) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secretKey);
        } catch (IllegalArgumentException e) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 64) {
            throw new IllegalArgumentException("JWT secret must be at least 64 bytes for HS512");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
