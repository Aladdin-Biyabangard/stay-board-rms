package az.aladdin.stayboard.exception;

import az.aladdin.stayboard.model.response.ApiErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final LocalizedMessageResolver localizedMessageResolver;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex) {
        return buildResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex) {
        return buildResponse(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return buildResponse(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException ex) {
        return buildResponse(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> formatFieldError(error, locale))
                .collect(Collectors.joining(", "));
        String message = localizedMessageResolver.resolve(MessageKey.VALIDATION, new Object[]{details}, locale);
        return ResponseEntity.badRequest().body(new ApiErrorResponse(ErrorCode.VALIDATION_ERROR, message));
    }

    private String formatFieldError(FieldError error, Locale locale) {
        String fieldLabel = localizedMessageResolver.resolve(FieldKey.of(error.getField()), null, locale);
        String constraintMessage = localizedMessageResolver.resolve(error.getDefaultMessage(), null, locale);
        return localizedMessageResolver.resolve(
                MessageKey.VALIDATION_FIELD,
                new Object[]{fieldLabel, constraintMessage},
                locale
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(LocalizedApiException ex, HttpStatus status) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = localizedMessageResolver.resolve(ex.getMessageKey(), ex.getArgs(), locale);
        return ResponseEntity.status(status).body(new ApiErrorResponse(ex.getCode(), message));
    }
}
