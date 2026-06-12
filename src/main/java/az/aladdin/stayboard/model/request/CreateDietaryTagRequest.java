package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotBlank;

public record CreateDietaryTagRequest(
        @NotBlank String tagName,
        String description,
        boolean active
) {
}
