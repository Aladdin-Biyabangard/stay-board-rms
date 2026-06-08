package az.aladdin.stayboard.exception;

public final class ApiExceptions {

    private ApiExceptions() {
    }

    public static NotFoundException notFound(String entityKey) {
        return new NotFoundException(entityKey);
    }

    public static BadRequestException badRequest(String messageKey) {
        return new BadRequestException(messageKey);
    }

    public static BadRequestException badRequest(String messageKey, Object... args) {
        return new BadRequestException(messageKey, args);
    }

    public static ConflictException conflict(String messageKey) {
        return new ConflictException(messageKey);
    }

    public static ConflictException conflict(String messageKey, Object... args) {
        return new ConflictException(messageKey, args);
    }

    public static UnauthorizedException unauthorized(String messageKey) {
        return new UnauthorizedException(messageKey);
    }

    public static ForbiddenException forbidden(String messageKey) {
        return new ForbiddenException(messageKey);
    }

    public static NotFoundException imageNotInGallery(String entityKey, String imageUrl) {
        return new NotFoundException(MessageKey.NOT_FOUND_IMAGE_NOT_IN_GALLERY, entityKey, imageUrl);
    }
}
