package az.aladdin.stayboard.exception;

public final class FieldKey {

    public static final String PREFIX = "field.";

    private FieldKey() {
    }

    public static String of(String fieldName) {
        return PREFIX + fieldName;
    }
}
