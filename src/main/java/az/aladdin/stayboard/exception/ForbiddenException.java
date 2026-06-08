package az.aladdin.stayboard.exception;

public class ForbiddenException extends LocalizedApiException {

    public ForbiddenException(String messageKey) {
        super(ErrorCode.FORBIDDEN, messageKey, null);
    }
}
