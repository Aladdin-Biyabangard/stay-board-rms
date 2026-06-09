package az.aladdin.stayboard.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final TraceFilter traceFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HotelContextFilter hotelContextFilter;
    private final LocalizedSecurityErrorHandler localizedSecurityErrorHandler;

    @Value("${security.cors.allowed-origins}")
    private List<String> allowedOriginPatterns;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(traceFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, TraceFilter.class)
                .addFilterAfter(hotelContextFilter, JwtAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    for (ApiEndpoint endpoint : ApiEndpoint.values()) {
                        if (List.of(endpoint.getSecurityLevels()).contains(ApiSecurityLevel.PUBLIC)) {
                            if (endpoint.getHttpMethod() == null) {
                                authorize.requestMatchers(endpoint.getPathPattern()).permitAll();
                            } else {
                                authorize.requestMatchers(endpoint.getHttpMethod(), endpoint.getPathPattern()).permitAll();
                            }
                        }
                    }

                    for (ApiEndpoint endpoint : ApiEndpoint.values()) {
                        List<ApiSecurityLevel> levels = List.of(endpoint.getSecurityLevels());
                        if (!levels.contains(ApiSecurityLevel.PUBLIC)) {
                            String[] roles = levels.stream()
                                    .map(Enum::name)
                                    .toArray(String[]::new);

                            if (endpoint.getHttpMethod() == null) {
                                authorize.requestMatchers(endpoint.getPathPattern()).hasAnyRole(roles);
                            } else {
                                authorize.requestMatchers(endpoint.getHttpMethod(), endpoint.getPathPattern()).hasAnyRole(roles);
                            }
                        }
                    }

                    // Guest users may only access endpoints that explicitly include GUEST in ApiEndpoint.
                    authorize.anyRequest().hasAnyRole(ApiEndpoint.staffRoleNames());
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(localizedSecurityErrorHandler)
                        .accessDeniedHandler(localizedSecurityErrorHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        Set<String> originPatterns = new LinkedHashSet<>(allowedOriginPatterns);
        config.setAllowedOriginPatterns(new ArrayList<>(originPatterns));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Link", "X-Total-Count", "X-Trace-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
