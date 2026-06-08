package az.aladdin.stayboard.config.security;

import az.aladdin.stayboard.exception.ErrorCode;
import az.aladdin.stayboard.exception.LocalizedMessageResolver;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.model.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocalizedSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final LocalizedMessageResolver localizedMessageResolver;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, MessageKey.UNAUTHORIZED);
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        writeError(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN, MessageKey.FORBIDDEN);
    }

    private void writeError(HttpServletResponse response, int status, String code, String messageKey) throws IOException {
        Locale locale = LocaleContextHolder.getLocale();
        String message = localizedMessageResolver.resolve(messageKey, null, locale);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(code, message));
    }
}
