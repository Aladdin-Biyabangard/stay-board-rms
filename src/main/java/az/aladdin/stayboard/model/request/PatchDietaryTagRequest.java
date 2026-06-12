package az.aladdin.stayboard.model.request;

public record PatchDietaryTagRequest(
        String tagName,
        String description,
        Boolean active
) {
}
