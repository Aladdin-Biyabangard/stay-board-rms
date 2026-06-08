package az.aladdin.stayboard.exception;

import lombok.Getter;

@Getter
public abstract class LocalizedApiException extends RuntimeException {

    private final String code;
    private final String messageKey;
    private final Object[] args;

    protected LocalizedApiException(String code, String messageKey, Object[] args) {
        super(messageKey);
        this.code = code;
        this.messageKey = messageKey;
        this.args = args != null ? args : new Object[0];
    }
}
