package az.aladdin.stayboard.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class HttpRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientIp = ClientIpResolver.resolve(request);
        boolean authPath = isAuthPath(request.getRequestURI());
        Bucket bucket = resolveBucket(clientIp, authPath);
        if (!bucket.tryConsume(1)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private Bucket resolveBucket(String clientIp, boolean authPath) {
        Map<String, Bucket> store = authPath ? authBuckets : generalBuckets;
        return store.computeIfAbsent(clientIp, ignored -> newBucket(authPath));
    }

    private Bucket newBucket(boolean authPath) {
        int perMinute = authPath ? properties.getAuthRequestsPerMinute() : properties.getGeneralRequestsPerMinute();
        int capacity = Math.max(properties.getBurstCapacity(), perMinute);
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(perMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private static boolean isAuthPath(String path) {
        return path.startsWith("/v1/rms/internal/db/");
    }
}
