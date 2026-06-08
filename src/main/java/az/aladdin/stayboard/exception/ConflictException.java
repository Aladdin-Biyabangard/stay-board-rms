package az.aladdin.stayboard.exception;

public class ConflictException extends LocalizedApiException {

    public ConflictException(String messageKey) {
        this(messageKey, null);
    }

    public ConflictException(String messageKey, Object[] args) {
        super(ErrorCode.CONFLICT, messageKey, args);
    }
}
