package az.aladdin.stayboard.config;

import az.aladdin.stayboard.context.HotelContextHolder;
import az.aladdin.stayboard.security.AuthenticatedUserSupport;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class PmsServiceFeignAuthConfig {

    public static final String INTERNAL_KEY_HEADER = "X-Stayboard-Internal-Key";
    public static final String HOTEL_ID_HEADER = "X-Hotel-Id";
    public static final String PERFORMED_BY_USER_ID_HEADER = "X-Performed-By-User-Id";

    @Bean
    public RequestInterceptor pmsServiceAuthForwardInterceptor(PmsServiceAuthProperties pmsServiceAuthProperties) {
        return template -> {
            // Guest portal: forward the guest JWT — no internal API key required.
            if (AuthenticatedUserSupport.isGuest()) {
                forwardIncomingAuthorization(template);
                return;
            }

            if (applyInternalServiceAuth(template, pmsServiceAuthProperties)) {
                return;
            }

            if (applyServiceToken(template, pmsServiceAuthProperties)) {
                return;
            }

            forwardIncomingAuthorization(template);
        };
    }

    private static boolean applyInternalServiceAuth(
            feign.RequestTemplate template,
            PmsServiceAuthProperties properties
    ) {
        if (!properties.hasInternalApiKey()) {
            return false;
        }

        Long hotelId = HotelContextHolder.getHotelId();
        if (hotelId == null) {
            return false;
        }

        template.header(INTERNAL_KEY_HEADER, properties.getInternalApiKey().trim());
        template.header(HOTEL_ID_HEADER, String.valueOf(hotelId));
        AuthenticatedUserSupport.currentPrincipal()
                .map(principal -> principal.getUserId())
                .ifPresent(userId -> template.header(PERFORMED_BY_USER_ID_HEADER, String.valueOf(userId)));
        return true;
    }

    private static boolean applyServiceToken(feign.RequestTemplate template, PmsServiceAuthProperties properties) {
        if (!properties.hasServiceToken()) {
            return false;
        }
        template.header("Authorization", normalizeBearerToken(properties.getServiceToken()));
        return true;
    }

    private static void forwardIncomingAuthorization(feign.RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        String authorization = attributes.getRequest().getHeader("Authorization");
        if (authorization != null && !authorization.isBlank()) {
            template.header("Authorization", authorization);
        }
    }

    private static String normalizeBearerToken(String token) {
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
