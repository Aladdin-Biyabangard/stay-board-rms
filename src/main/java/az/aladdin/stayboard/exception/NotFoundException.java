package az.aladdin.stayboard.exception;

public class NotFoundException extends LocalizedApiException {

    public NotFoundException(String entityMessageKey) {
        super(ErrorCode.NOT_FOUND, MessageKey.NOT_FOUND, new Object[]{entityMessageKey});
    }

    public NotFoundException(String messageKey, Object... args) {
        super(ErrorCode.NOT_FOUND, messageKey, args);
    }
}
