package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAllergenRequest(
        @NotBlank String allergenName,
        String description,
        boolean active
) {
}
