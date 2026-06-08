package az.aladdin.stayboard.exception;

public class UnauthorizedException extends LocalizedApiException {

    public UnauthorizedException(String messageKey) {
        super(ErrorCode.UNAUTHORIZED, messageKey, null);
    }
}
