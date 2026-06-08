package az.aladdin.stayboard.exception;

public class BadRequestException extends LocalizedApiException {

    public BadRequestException(String messageKey) {
        this(messageKey, null);
    }

    public BadRequestException(String messageKey, Object[] args) {
        super(ErrorCode.BAD_REQUEST, messageKey, args);
    }
}
