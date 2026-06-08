package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateMenuCategoryRequest(
        @NotBlank String categoryName,
        String description
) {
}
