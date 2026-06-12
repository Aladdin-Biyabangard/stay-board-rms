package az.aladdin.stayboard.model.request;

public record PatchAllergenRequest(
        String allergenName,
        String description,
        Boolean active
) {
}
